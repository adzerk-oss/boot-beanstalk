# boot-beanstalk

Boot task to deploy artifacts to Amazon's Elastic Beanstalk.

[![Clojars Project][1]][2]

## Options

To see a list of options accepted by this task:

```
$ boot beanstalk -h
```

## Usage

Sample `build.boot` (deploys a ring app to Elastic Beanstalk as either a war
file to run in Tomcat or a docker image):

```clojure
(set-env!
  :target-path   "target"
  :resource-paths  #{"src"}
  :dependencies '[[org.clojure/clojure "1.6.0"]
                  [adzerk/boot-beanstalk "X.Y.Z"]])

(require '[adzerk.boot-beanstalk :refer [beanstalk dockerrun]])

(task-options!
  web       {:serve          'my-application.core/handler}
  beanstalk {:name           "my-application"
             :version        "0.1.0-SNAPSHOT"
             :description    "My awesome application"
             :access-key     (System/getenv "AWS_ACCESS_KEY")
             :secret-key     (System/getenv "AWS_SECRET_KEY")
             :beanstalk-envs [{;; name must be unique in AWS account
                               :name "my-application-dev"
                               ;; http://<cname-prefix>.elasticbeanstalk.com
                               :cname-prefix "my-application-dev"}]})
  
(deftask build-tomcat
  "Build my application uberwar file."
  []
  (comp (web) (uber) (war)))

(deftask build-docker
  "Build my application docker zip file."
  []
  (comp (add-repo) (dockerrun) (zip)))
  
(deftask deploy-tomcat
  "Deploy application war file to AWS EB environment."
  []
  (task-options!
    beanstalk {:stack-name "64bit Amazon Linux 2014.03 v1.0.7 running Tomcat 7 Java 7"})
  identity)
  
(deftask deploy-docker
  "Deploy application docker zip file to AWS EB environment."
  []
  (task-options!
    beanstalk {:stack-name "64bit Amazon Linux 2014.09 v1.0.9 running Docker 1.2.0"})
  identity)
```

The `:stack-name` option may need to be updated, as Amazon changes those all the
time. You can see a list of available "solution stacks" by doing:

```
$ boot beanstalk -l
```

Let's build and deploy the application!

#### Tomcat

Build the war file:

```
$ boot build-tomcat
```

and create or update Elastic Beanstalk environment:

```
$ boot deploy-tomcat beanstalk -f project.war -de my-application-dev
```

#### Docker

**Note:** Your application will need to have a [Dockerfile][3] to build
the docker image. Also make sure that all the files you want to include
in the image are being tracked by `git`. Tracked files in the project
directory will be included in the zip file and uploaded to S3.

Build the docker zip file:

```
$ boot build-docker
```

and create or update Elastic Beanstalk environment:

```
$ boot deploy-docker beanstalk -f project.zip -de my-application-dev
```

## License

Copyright © 2015 Adzerk

Distributed under the Eclipse Public License either version 1.0 or (at
your option) any later version.

[1]: http://clojars.org/adzerk/boot-beanstalk/latest-version.svg?1
[2]: http://clojars.org/adzerk/boot-beanstalk
[3]: https://docs.docker.com/reference/builder/
