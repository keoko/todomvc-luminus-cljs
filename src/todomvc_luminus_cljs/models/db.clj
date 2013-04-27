(ns todomvc-luminus-cljs.models.db
  (:use korma.core
        [korma.db :only (defdb)])
  (:require [todomvc-luminus-cljs.models.schema :as schema]))

(defdb db schema/db-spec)

(defentity todolist)

;; helper function to get the row id after running an insert query
(defn get-id [response]
  (first (vals response)))


(defn create-todo
  [title completed]
  (insert todolist
          (values {:title title
                   :completed completed})))

(defn list-todos []
  (select todolist))

(defn get-todo [id]
  (first (select todolist (where {:id id}))))

(defn delete-todo [id]
  (delete todolist (where {:id id})))

(defn delete-completed  []
  (delete todolist (where {:completed true})))

(defn update-todo [id title completed]
  (update todolist
          (set-fields {:title title
                       :completed completed})
          (where {:id id})))