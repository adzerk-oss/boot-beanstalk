(ns adzerk.boot-beanstalk
  (:require
   [boot.pod  :as pod]
   [boot.core :as boot]))

(def bs-dep '[lein-beanstalk "0.2.7"])

(boot/deftask beanstalk
  "Deploy war file to elastic beanstalk."

  [f file PATH          str  "The path to the deployable artifact."
   A access-key KEY     str  "The AWS API access key."
   S secret-key KEY     str  "The AWS API secret key."
   n name NAME          str  "The application name."
   D description DESC   str  "The application description."
   v version VER        str  "The application version."
   e env NAME           str  "The (optional) environment name."
   s stack-name STACK   str  "The (optional) beanstalk solution stack name."
   l list-stacks        bool "Print a list of available solution stack names."
   c clean              bool "Clean out old versions, except the ones currently deployed."
   d deploy             bool "Deploy the current project to Amazon Elastic Beanstalk."
   i info               bool "Print info about the project on Amazon Elastic Beanstalk."
   t terminate          bool "Terminate the environment for the current project on Amazon Elastic Beanstalk."]
  
  (if-not (or clean deploy info terminate list-stacks)
    (beanstalk :help true)
    (let [p (-> (boot/get-env) (update-in [:dependencies] conj bs-dep)
              pod/make-pod
              future)]
      (boot/with-pre-wrap
        (pod/call-in @p
          `(adzerk.boot-beanstalk.impl/beanstalk ~*opts*))))))
