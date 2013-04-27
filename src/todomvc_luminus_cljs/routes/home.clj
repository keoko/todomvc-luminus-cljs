(ns todomvc-luminus-cljs.routes.home
  (:use compojure.core
        [taoensso.timbre :only [trace debug info warn error fatal]])
  (:require [todomvc-luminus-cljs.views.layout :as layout]
            [todomvc-luminus-cljs.util :as util]
            [todomvc-luminus-cljs.models.db :as db]
            [noir.response :as resp]))
           

(defn home-page []
  (layout/render "index.html"))

(defn list-todos []
  (resp/edn (db/list-todos)))

(defn get-todo [id]
  (if-let [todo-resource (resp/edn (db/get-todo id))]
    todo-resource
    {:status 404}))

(defn delete-todo [id]
  (db/delete-todo id)
  (resp/edn {:id 0}))

(defn delete-completed []
  (db/delete-completed)
  (resp/edn {:id 0}))

(defn update-todo [id title completed]
  (db/update-todo id title completed)
  (get-todo id))

(defn create-todo [title completed]
  (when-let [id (db/get-id (db/create-todo title completed))]
    (get-todo id)))


(defroutes home-routes
  (GET "/" [] (home-page))
  (GET "/todos" [] (list-todos))
  (POST "/todos" [title completed] (create-todo title completed))
  (DELETE "/todos/completed" [] (delete-completed))
  (DELETE "/todos/:id" [id] (delete-todo id))
  (GET "/todos/:id" [id] (get-todo id))
  (PUT "/todos/:id" [id title completed] (update-todo id title completed)))