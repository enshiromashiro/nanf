(ns ui.swing
  (:require [clojure.string :as string])
  (:import (java.awt Dimension
                     GridBagConstraints
                     GridBagLayout)
           (java.io File)
           (javax.swing JFrame
                        JFileChooser
                        JPanel
                        UIManager)
           (javax.swing.filechooser FileNameExtensionFilter)))



(defn member-sym [sym]
  (symbol (str "." (string/replace (name sym) "*" ""))))

(defmacro java-set! [sym val]
  (when val
    `(set! ~sym ~val)))


(defmacro with-jframe [[v t] & body]
  "Make JFrame and bind it to _v_."
  `(let [~v (JFrame. ~t)]
     (UIManager/setLookAndFeel
      (UIManager/getSystemLookAndFeelClassName))
     (System/setProperty "awt.useSystemAAFontSettings" "on")
     ~@body
     (.setDefaultCloseOperation ~v JFrame/DISPOSE_ON_CLOSE)
     (.setVisible ~v true)))

(defmacro jpanel [vmap]
  (let [panel (gensym 'panel)]
    `(let [~panel (JPanel.)]
       ~@(for [p vmap] `(~(member-sym (first p))
                         ~panel
                         ~(second p)))
       ~panel)))

(defn dim [w h]
  (Dimension. w h))


;;; GridBagLayout and GridBagConstraints
(def +gbc-constants+
  {:both GridBagConstraints/BOTH
   :horizontal GridBagConstraints/HORIZONTAL})

(def +gbc-fields+
  [:gridx :gridy :gridheight :gridwidth
   :weightx :weighty :ipadx :ipady
   :anchor :fill :insets])

(defmacro make-gb-constraints [vmap]
  (let [gbc (gensym 'gbc)]
    `(let [~gbc (GridBagConstraints.)]
       ~@(for [p vmap] `(set! (~(member-sym (first p)) ~gbc)
                              ~(if (keyword? (second p))
                                 (+gbc-constants+ (second p))
                                 (second p))))
       ~gbc)))

(defmacro gridbag-layout [& pairseq]
  "Set GridBagConstraints to GridBagLayout and return the GridBagLayout.
It takes a sequens of pairs.
The pairs must be consist of a keyword named GridBagConstraints property and its value."
  (let [layout (gensym 'layout)]
    `(let [~layout (GridBagLayout.)]
       ~@(for [p pairseq]
         `(.setConstraints ~layout ~(first p)
                           (make-gb-constraints ~(second p))))
      ~layout)))


(defmacro add-listener [[event-name component] & methods]
  (let [add-listener (symbol (str ".add" (name event-name) "Listener"))
        adapter-name (symbol (str "java.awt.event."
                                  (name event-name) "Adapter"))]
    `(~add-listener ~component
                    (proxy [~adapter-name] [] ~@methods))))


(defn show-open-chooser [frame desc exts]
  "Display an open dialog, then return selected file."
  (let [c (JFileChooser.)]
    (.setFileFilter c
                    (FileNameExtensionFilter. desc
                                              (into-array exts)))
    (let [ret (.showOpenDialog c frame)]
      (if (= ret JFileChooser/APPROVE_OPTION)
        (.getAbsolutePath (.getSelectedFile c))
        nil))))

