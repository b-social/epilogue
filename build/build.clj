(ns build
  (:require [clojure.tools.build.api :as b]
            [deps-deploy.deps-deploy :as dd]))

(def lib 'com.kroo/epilogue)

(def version
  (if-let [tag (b/git-process {:git-args ["tag" "--points-at" "HEAD"]})]
    (if (= \v (first tag))
      (subs tag 1)
      tag)
    "local"))

(def basis (b/create-basis {:project "deps.edn"}))
(def class-dir "target/classes")
(def jar-file (format "target/%s-%s.jar" (name lib) version))

(defn clean
  "Clean the targets folder."
  [_opts]
  (b/delete {:path "target"}))

(defn jar
  "Build the JAR."
  [_opts]
  (clean nil)
  (b/copy-dir {:src-dirs ["src" "resources"]
               :target-dir class-dir})
  (b/write-pom {:class-dir     class-dir
                :lib           lib
                :version       version
                :basis         basis
                :resource-dirs ["resources"]
                :src-dirs      ["src"]
                :src-pom       "build/pom.xml"})
  (b/jar {:class-dir class-dir
          :jar-file  jar-file}))

(defn install
  "Install the JAR locally."
  [_opts]
  (b/install
    {:basis      basis
     :lib        lib
     :version    version
     :jar-file   jar-file
     :class-dir  class-dir}))

(defn deploy
  "Deploy the JAR to Clojars."
  [_opts]
  (dd/deploy {:installer :remote
              :artifact (b/resolve-path jar-file)
              :pom-file (b/pom-path {:lib lib, :class-dir class-dir})}))
