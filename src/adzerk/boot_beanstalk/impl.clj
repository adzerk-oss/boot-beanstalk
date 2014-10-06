(ns adzerk.boot-beanstalk.impl
  (:require
   [clojure.java.io         :as io]
   [clojure.string          :as str]
   [clojure.set             :as set]
   [boot.pod                :as pod]
   [boot.util               :as util]
   [leiningen.beanstalk.aws :as aws]))

(defn default-environments
  [project]
  (let [project-name (:name project)]
    [{:name "development" :cname-prefix (str project-name "-dev")}
     {:name "staging"     :cname-prefix (str project-name "-staging")}
     {:name "production"  :cname-prefix project-name}]))

(defn project-environments
  [project]
  (for [env (-> project :aws :beanstalk :environments)]
    (if (map? env)
      (merge {:cname-prefix (str (:name project) "-" (:name env))} env)
      {:name env, :cname-prefix (str (:name project) "-" env)})))

(defn get-project-env [project env-name]
  (->> (or (seq (project-environments project))
           (default-environments project))
       (filter #(= (:name %) env-name))
       (first)))

(defn do-deploy
  "Deploy the current project to Amazon Elastic Beanstalk."
  [project env-name]
  (if-let [env (get-project-env project env-name)]
    (let [path (:war-path project)]
      (aws/s3-upload-file project path)
      (aws/create-app-version project (.getName (io/file path)))
      (aws/deploy-environment project env))
    (println (str "Environment '" env-name "' not defined!"))))

(defn do-terminate
  "Terminte the environment for the current project on Amazon Elastic Beanstalk."
  [project env-name]
  (if-not (get-project-env project env-name)
    (println (str "Environment '" env-name "' not in project.clj"))
    (aws/terminate-environment project env-name)))

(def app-info-indent "\n                  ")

(defn- last-versions-info [app]
  (str/join app-info-indent (take 5 (.getVersions app))))

(defn- deployed-envs-info [project]
  (str/join
    app-info-indent
    (for [env (aws/app-environments project)]
      (str (.getEnvironmentName env) " (" (.getStatus env) ")"))))

(defn app-info
  "Displays information about a Beanstalk application."
  [project]
  (if-let [app (aws/get-application project)]
    (println (str "Application Name: " (.getApplicationName app) "\n"
                  "Last 5 Versions:  " (last-versions-info app) "\n"
                  "Created On:       " (.getDateCreated app) "\n"
                  "Updated On:       " (.getDateUpdated app) "\n"
                  "Deployed Envs:    " (deployed-envs-info project)))
    (println (str "Application '" (:name project) "' "
                  "not found on AWS Elastic Beanstalk"))))

(defn env-info
  "Displays information about a Beanstalk environment."
  [project env-name]
  (if-let [env (aws/get-env project env-name)]
    (println (str "Environment ID:    " (.getEnvironmentId env) "\n"
                  "Application Name:  " (.getApplicationName env) "\n"
                  "Environment Name:  " (.getEnvironmentName env) "\n"
                  "Description:       " (.getDescription env) "\n"
                  "URL:               " (.getCNAME env) "\n"
                  "Load Balancer URL: " (.getEndpointURL env) "\n"
                  "Status:            " (.getStatus env) "\n"
                  "Health:            " (.getHealth env) "\n"
                  "Current Version:   " (.getVersionLabel env) "\n"
                  "Solution Stack:    " (.getSolutionStackName env) "\n"
                  "Created On:        " (.getDateCreated env) "\n"
                  "Updated On:        " (.getDateUpdated env)))
    (println (str "Environment '" env-name "' "
                  "not found on AWS Elastic Beanstalk"))))

(defn do-info
  "Provides info for about project on Amazon Elastic Beanstalk."
  ([project]
     (app-info project))
  ([project env-name]
     (if-not (get-project-env project env-name)
       (println (str "Environment '" env-name "' not defined!"))
       (env-info project env-name))))

(defn do-clean
  "Cleans out old versions, except the ones currently deployed."
  [project]
  (let [all-versions      (set (.getVersions (aws/get-application project)))
        deployed-versions (set (map #(.getVersionLabel %)
                                    (aws/app-environments project)))]
    (doseq [version (set/difference all-versions deployed-versions)]
      (print (str "Removing '" version "'"))
      (aws/delete-app-version project version)
      (print (str " -> done!\n")))))

(defn do-list-stacks
  "Prints a list the available solution stack names."
  [project]
  (doseq [s (-> (#'aws/beanstalk-client project)
              .listAvailableSolutionStacks
              .getSolutionStacks)]
    (println s)))

(defn beanstalk
  "Deploy war file to elastic beanstalk."
  [{:keys [name version description file env
           clean deploy info terminate list-stacks
           access-key secret-key s3-bucket aws-region stack-name]
    :as opts}]
  (let [project {:name        name
                 :version     version
                 :description description
                 :war-path    file
                 :aws         {:access-key access-key
                               :secret-key secret-key}}
        project (if-not stack-name project
                  (update-in project [:aws :beanstalk] assoc :stack-name stack-name))
        project (if-not aws-region project
                  (update-in project [:aws :beanstalk] assoc :region aws-region))
        project (if-not s3-bucket project
                  (update-in project [:aws :beanstalk] assoc :s3-bucket s3-bucket))]
    (assert (or (not (or deploy terminate)) env)
      "You must specify an environment.")
    (cond
      clean       (do-clean project)
      deploy      (do-deploy project env)
      info        (apply do-info project (when env [env]))
      terminate   (do-terminate project env)
      list-stacks (do-list-stacks project))))
