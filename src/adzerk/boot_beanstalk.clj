(ns adzerk.boot-beanstalk
  (:require
   [clojure.java.io :as io]
   [boot.pod        :as pod]
   [boot.core       :as boot]))

(def bs-deps '[[lein-beanstalk "0.2.7"]])

(boot/deftask dockerrun
  "Create Dockerrun.aws.json file.

  The default log directory is /var/log/nginx."

  [v volumes HOST:CTNR {str str} "The map of host to container directories."
   l logging PATH      str       "The log directory to map in the container."]

  (let [tgt (boot/mktgtdir!)]
    (boot/with-pre-wrap
      (let [out   (io/file tgt "Dockerrun.aws.json")
            ->vol (fn [[h c]] {"HostDirectory" h "ContainerDirectory" c})]
        (spit out (boot/json-generate {"AWSEBDockerrunVersion" "1"
                                       "Volumes" (mapv ->vol volumes)
                                       "Logging" (or logging "/var/log/nginx")}))))))

(boot/deftask beanstalk
  "AWS Elastic Beanstalk environment management."

  [f file PATH          str  "The path to the deployable artifact."
   A access-key KEY     str  "The AWS API access key."
   S secret-key KEY     str  "The AWS API secret key."
   n name NAME          str  "The application name."
   D description DESC   str  "The application description."
   v version VER        str  "The application version."
   r aws-region REGION  str  "The (optional) region to deploy the application."
   e env NAME           str  "The (optional) environment name."
   s stack-name STACK   str  "The (optional) beanstalk solution stack name."
   b s3-bucket BUCKET   str  "The (optional) S3 bucket where artifacts will be saved."
   l list-stacks        bool "Print a list of available solution stack names."
   c clean              bool "Clean out old versions, except the ones currently deployed."
   d deploy             bool "Deploy the current project to Amazon Elastic Beanstalk."
   i info               bool "Print info about the project on Amazon Elastic Beanstalk."
   t terminate          bool "Terminate the environment for the current project on Amazon Elastic Beanstalk."]

  (if-not (or clean deploy info terminate list-stacks)
    (beanstalk :help true)
    (let [p (-> (boot/get-env)
              (update-in [:dependencies] into bs-deps)
              pod/make-pod
              future)]
      (boot/with-pre-wrap
        (pod/call-in @p
          `(adzerk.boot-beanstalk.impl/beanstalk ~*opts*))))))
