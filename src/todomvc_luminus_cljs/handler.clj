(ns todomvc-luminus-cljs.handler
  (:use todomvc-luminus-cljs.routes.home compojure.core)
  (:require [noir.util.middleware :as middleware]
            [compojure.route :as route]
            [todomvc-luminus-cljs.routes.cljsexample :refer [cljs-routes]]))

(defroutes
  app-routes
  (route/resources "/")
  (route/not-found "Not Found"))

(defn init
  "init will be called once when\n   app is deployed as a servlet on\n   an app server such as Tomcat\n   put any initialization code here"
  []
  (println "todomvc-luminus-cljs started successfully..."))

(defn destroy
  "destroy will be called when your application\n   shuts down, put any clean up code here"
  []
  (println "shutting down..."))

(def all-routes [cljs-routes home-routes app-routes])

(def app (-> all-routes middleware/app-handler))

(def war-handler (middleware/war-handler app))

