# boot-beanstalk

Boot task to deploy artifacts to Amazon's Elastic Beanstalk.

## Example Usage

Sample `build.boot`:

```clojure
(set-env!
  :src-paths #{"src"}
  :dependencies '[[adzerk/boot-beanstalk "X.Y.Z"]])

(require '[adzerk.boot-beanstalk :refer [beanstalk]])

(task-options!
  web       [:serve       'my-application.core/handler]
  beanstalk [:name        "my-application"
             :version     "0.1.0-SNAPSHOT"
             :description "My awesome application"
             :stack-name  "64bit Amazon Linux 2014.03 v1.0.7 running Tomcat 7 Java 7"
             :access-key  (System/getenv "AWS_ACCESS_KEY")
             :secret-key  (System/getenv "AWS_SECRET_KEY")])
  
(deftask build
  "Build my application war file."
  []
  (comp (add-src) (web) (uber) (war)))
```

Then build the war file:

```
$ boot build
```

Finally create or update Elastic Beanstalk environments for the application:

```
$ boot beanstalk -f target/project.war -e development
```

Or get info about deployed environments for the application:

```
$ boot beanstalk -i
```

Or info about a specific environment:

```
$ boot beanstalk -i -e development
```

## License

Copyright © 2014 Adzerk

Distributed under the Eclipse Public License either version 1.0 or (at
your option) any later version.
