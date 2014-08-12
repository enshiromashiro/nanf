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
   :run "解析"})


(defn run-nan [fpath & opts]
  "Execute nan"
  (sh (.getAbsolutePath (file (executable-name +nan-bin+)))
      fpath opts))

(defn -main [& args]
  "nanf main"
  (with-jframe [frame (:title +window-text+)]
    (let [path-txt (JTextField.)
          result-txt (JTextArea. ";; ここに結果が出ます")
          result-pane (JScrollPane. result-txt
                                    JScrollPane/VERTICAL_SCROLLBAR_ALWAYS
                                    JScrollPane/HORIZONTAL_SCROLLBAR_NEVER)
          open-btn (JButton. (:open +window-text+))
          run-btn (JButton. (:run +window-text+))]
      (add-listener [Mouse open-btn]
        (mouseClicked [e]
          (.setText path-txt (show-open-chooser frame
                                                "text file (*.txt)"
                                                ["txt"]))))
      (add-listener [Mouse run-btn]
        (mouseClicked [e]
          (.append result-txt
                   (str (:out (run-nan (.getText path-txt)))))))
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
                                   [result-pane
                                    {:gridx 0 :gridy 1
                                     :weightx 1 :weighty 1
                                     :gridwidth 3
                                     :fill :both}]
                                   [path-txt
                                    {:gridx 0 :gridy 0
                                     :weightx 1 :weighty 0
                                     :gridwidth 1
                                     :fill :horizontal}])
                       :setPreferredSize (dim +window-width+ +window-height+)}))
        (.pack)))))
