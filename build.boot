(set-env!
  :src-paths    #{"src"}
  :dependencies '[[org.clojure/clojure "1.6.0"      :scope "provided"]
                  [boot/core           "2.0.0-pre5" :scope "provided"]
                  [lein-beanstalk      "0.2.7"      :scope "test"]])

(task-options!
  pom [:project     'adzerk/boot-beanstalk
       :version     "0.1.0-SNAPSHOT"
       :description "FIXME: write description"
       :url         "http://example.com/FIXME"
       :license     {:name "Eclipse Public License"
                     :url  "http://www.eclipse.org/legal/epl-v10.html"}])

(require '[adzerk.boot-beanstalk :as bs])

(deftask build
  "Build jar and install to local repo."
  []
  (comp (pom) (add-src) (jar) (install)))
