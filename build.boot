(set-env!
  :src-paths    #{"src"}
  :dependencies '[[org.clojure/clojure       "1.6.0" :scope "provided"]
                  [tailrecursion/boot-useful "0.1.3" :scope "test"]
                  [lein-beanstalk            "0.2.7" :scope "test"]])

(require '[tailrecursion.boot-useful :refer :all])

(def +version+ "0.1.0")
(useful! +version+)

(task-options!
  pom [:project     'adzerk/boot-beanstalk
       :version     +version+
       :description "Boot task to deploy artifacts to Amazon Elastic Beanstalk."
       :url         "https://github.com/adzerk/boot-beanstalk"
       :scm         {:url "https://github.com/adzerk/boot-beanstalk"}
       :license     {:name "Eclipse Public License"
                     :url  "http://www.eclipse.org/legal/epl-v10.html"}])
