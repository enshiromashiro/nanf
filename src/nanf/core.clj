(ns nanf.core
  (:gen-class)
  (:use [clojure.java.shell
         :only [sh]]
        [ clojure.java.io
         :only [file]]
        [ui.swing
         :only [add-listener
                dim
                gridbag-layout
                jpanel
                show-open-chooser
                with-jframe]]
        [util.util
         :only [windows?
                executable-name]])
  (:import (java.io File)
           (javax.swing JButton
                        JScrollPane
                        JTextArea
                        JTextField)))



(def +nan-bin+ "nan")
(def +nan-items+
  {:papers "原稿用紙枚数"
   :chars "全文字数"
   :lines "行数"
   :nw-num "地の文/台詞(文字数)"
   :nw-ratio "地の文/台詞(%)"
   :kjhko-num "漢字/ひらがな/カタカナ/その他(文字数)"
   :kjhko-ratio "漢字/ひらがな/カタカナ/その他(%)"
   :line-head-indent "行頭字下げ"
   :close-paren "括弧閉じ前に句読点"
   :!?blank "！？後に全角ペース"
   :ellipsis "三点リーダ連続"
   :dash "ダッシュ連続"})

(def +window-width+ 640)
(def +window-height+ 480)
(def +window-text+
  {:title "nanf"
   :open "開く"
   :run "解析"
   :init ";; ここに結果が出ます\n"
   :error "\n[エラー]\n"
   :no-file "ファイルを選択してください\n"
   :no-nan "nanを同じディレクトリに置いてください\n"})


(defn run-nan [fpath]
  "Execute nan"
  (apply sh `(~(.getAbsolutePath (file (executable-name +nan-bin+)))
              ~fpath "-s")))

(defn error-msg [& strs]
  (apply str (concat (:error +window-text+) strs)))

(defn make-open-clicked [path frame]
  #(.setText path (show-open-chooser frame
                                     "text file (*.txt)"
                                     ["txt"])))


(defn make-run-clicked [path result]
  (letfn [(append [s] (.append result s))]
    #(let [p (.getText path)]
       (if (or (nil? p) (empty? p))
         (append (error-msg (:no-file +window-text+)))
         (try 
           (let [ret (run-nan p)]
             (if (and (empty? (:err ret)) (zero? (:exit ret)))
               (append (str "\n" (:out ret)))
               (append (error-msg "エラーコード: "
                                  (:exit ret)
                                  "\nエラーメッセージ: "
                                  (:err ret)))))
           (catch java.io.IOException e
             (append (error-msg (:error +window-text+)
                                (.getMessage e) "\n"
                                (:no-nan +window-text+)))))))))


(defn -main [& args]
  "nanf main"
  (with-jframe [frame (:title +window-text+)]
    (let [path-txt (JTextField.)
          result-txt (JTextArea. (:init +window-text+))
          result-pane (JScrollPane. result-txt
                                    JScrollPane/VERTICAL_SCROLLBAR_ALWAYS
                                    JScrollPane/HORIZONTAL_SCROLLBAR_NEVER)
          open-btn (JButton. (:open +window-text+))
          run-btn (JButton. (:run +window-text+))
          openclicked (make-open-clicked path-txt frame)
          runclicked (make-run-clicked path-txt result-txt)]
      (add-listener [Mouse open-btn]
        (mouseClicked [e] (openclicked)))
      (add-listener [Mouse run-btn]
        (mouseClicked [e] (runclicked)))
      (add-listener [Window frame]
        (windowClosed [e] (System/exit 0)))
      (doto result-txt
        (.setLineWrap true)
        (.setWrapStyleWord true)
        (.setEditable false))
      (doto frame
        (.add (jpanel {:add path-txt
                       :add* open-btn
                       :add** run-btn
                       :add*** result-pane
                       :setLayout (gridbag-layout
                                   [result-pane {:gridx 0 :gridy 1
                                                 :weightx 1 :weighty 1
                                                 :gridwidth 3
                                                 :fill :both}]
                                   [path-txt {:gridx 0 :gridy 0
                                              :weightx 1 :weighty 0
                                              :gridwidth 1
                                              :fill :horizontal}])
                       :setPreferredSize (dim +window-width+ +window-height+)}))
        (.pack)))))
