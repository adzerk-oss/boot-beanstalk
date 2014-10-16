(set-env!
  :src-paths    #{"src"}
  :repositories #(conj % ["deploy" {:url      "https://clojars.org/repo"
                                    :username (System/getenv "CLOJARS_USER")
                                    :password (System/getenv "CLOJARS_PASS")}])
  :dependencies '[[org.clojure/clojure "1.6.0"      :scope "provided"]
                  [boot/core           "2.0.0-pre5" :scope "provided"]
                  [lein-beanstalk      "0.2.7"      :scope "test"]])

(task-options!
  pom [:project     'adzerk/boot-beanstalk
       :version     "0.1.0-SNAPSHOT"
       :description "Boot task to deploy artifacts to Amazon Elastic Beanstalk."
       :url         "https://github.com/adzerk/boot-beanstalk"
       :scm         {:url "https://github.com/adzerk/boot-beanstalk"}
       :license     {:name "Eclipse Public License"
                     :url  "http://www.eclipse.org/legal/epl-v10.html"}])

(deftask build
  "Build jar and install to local repo."
  []
  (comp (pom) (add-src) (jar) (install)))
