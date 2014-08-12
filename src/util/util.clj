(ns util.util)



(defn windows? []
  "If on Windows, return true."
  (. (System/getProperty "os.name")
     String/startsWith "Windows"))

(defn executable-name [name]
  "Return platform-dependent name of executable."
  (if (windows?)
    (str name ".exe")
    name))

