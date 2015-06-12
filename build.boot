(set-env!
  :src-paths    #{"src"}
  :dependencies '[[adzerk/bootlaces      "0.1.8"     :scope "test"]
                  [adzerk/lein-beanstalk "0.2.8"     :scope "test"]])

(require '[adzerk.bootlaces :refer :all])

(def +version+ "0.6.1")
(bootlaces! +version+)

(task-options!
 pom {:project     'adzerk/boot-beanstalk
      :version     +version+
      :description "Boot task to deploy artifacts to Amazon Elastic Beanstalk."
      :url         "https://github.com/adzerk/boot-beanstalk"
      :scm         {:url "https://github.com/adzerk/boot-beanstalk"}
      :license     {"Eclipse Public License"
                    "http://www.eclipse.org/legal/epl-v10.html"}})
