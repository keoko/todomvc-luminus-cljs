(ns todomvc-luminus-cljs.models.schema
  (:require [clojure.java.jdbc :as sql]
            [noir.io :as io]))

(def db-store "site.db")

(def db-spec {:classname "org.h2.Driver"
              :subprotocol "h2"
              :subname (str (io/resource-path) db-store)
              :user "sa"
              :password ""
              :naming {:keys clojure.string/lower-case
                       :fields clojure.string/upper-case}})

(defn initialized?
  []
  (.exists (new java.io.File (str (io/resource-path) db-store ".h2.db"))))

(defn create-todolist-table []
  (sql/with-connection
    db-spec
    (sql/create-table
     :todolist
     [:id "INTEGER PRIMARY KEY AUTO_INCREMENT"]
     [:title "VARCHAR(200)"]
     [:completed "BOOLEAN"])
    (sql/do-commands
     "CREATE INDEX completed_index ON todolist (completed)")))

(defn create-tables
  []
  (create-todolist-table))