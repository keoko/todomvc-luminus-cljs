(ns todomvc-luminus-cljs.main
  (:require [ajax.core :refer [GET POST]] 
            [domina :refer [value by-id by-name destroy-children! append! set-attr!]]
            [domina.events :refer [listen! builtin-events]]
            [dommy.template :as template]
            [clojure.string :as string]))

(def todo-server "http://localhost:3000")
(def enter-key 13)
(def todos (atom []))
(def stat (atom {}))

;; helper functions as they are not yet implemented in cljs-ajax
(defn ajax-request
  [uri method {:keys [format handler error-handler params]}]
  (let [xmlhttp (js/XMLHttpRequest)
        js-params (string/join "&" (map (fn [[k,v]] (str (symbol k) "=" (js/encodeURIComponent v))) params))]
    (set! (.-onreadystatechange xmlhttp)
          (fn [] (when (and (==  4 (.-readyState xmlhttp))
                            (== 200 (.-status xmlhttp)))
                   (handler))))
    (.open xmlhttp method uri, true)
    (.setRequestHeader xmlhttp "Content-type" "application/x-www-form-urlencoded")
    (.send xmlhttp js-params)))

(defn PUT [uri opts]
  (ajax-request uri "PUT" opts))

(defn DELETE
  [uri & [opts]]
  (ajax-request uri "DELETE" opts))



;; core functions
(defn compute-stats []
  (.log js/console "compute stats")
  (let [total (count @todos)
        completed (count (filter :completed @todos))]
    (swap! stat (fn [_] {:total-todo (count @todos)
                         :todo-left (- total completed)
                         :todo-completed completed})))
  (.log js/console "stat" @stat))


(defn get-todo-by-id [id]
  (first (filter (fn [x] (== id (:id x))) @todos)))

(defn checkbox-change-handler [evt]
  (let [checkbox (:target evt)
        id (js/parseInt (.getAttribute  checkbox "data-todo-id"))
        todo (get-todo-by-id id)]
    (.log js/console "checkbox-change-handler")
    (edit-todo id (:title todo) (.-checked checkbox))))

(defn todo-content-handler [evt]
  (let [id (.getAttribute (:target evt) "data-todo-id")
        div (by-id (str "li_" id))
        input-edit-todo (by-id (str "input_" id))]
    (set-attr! div "class" "editing")
    (.focus input-edit-todo)))

(defn input-edit-todo-key-press-handler [evt]
  (let [input-edit-todo (:target evt)
        id (.slice (.-id input-edit-todo) 6)
        title (.-value input-edit-todo)]
    (when (= enter-key (:keyCode evt))
      (edit-todo id title false))))

(defn input-edit-todo-blur-handler [evt]
  (let [input-edit-todo (:target evt)
        id (.slice (.-id input-edit-todo) 6)]
    (edit-todo id (.-value input-edit-todo))))

(defn remove-todos-completed []
  (DELETE (str todo-server "/todos/completed") {:handler refresh-data :format :edn}))

(defn href-clear-click-handler []
  (remove-todos-completed))

(defn draw-todo-clear []
  (let [button-clear (template/node [:button {:id "clear-completed"} (str  "Clear completed (" (:todo-completed @stat) ")")])
        footer (by-id "footer")]
    (listen! button-clear :click href-clear-click-handler)
    (append! footer button-clear)))

(defn redraw-todo [{:keys [id title completed]}]
  (.log js/console "redraw-todo" id)
  (let [checkbox (template/node [:input {:class "toggle"
                                         :data-todo-id id
                                         :type "checkbox"}])
        label (template/node [:label {:data-todo-id id} title])
        delete-link (template/node [:button {:class "destroy" :data-todo-id id}])
        div-display (template/node [:div {:class "view" :data-todo-id id}])
        input-edit-todo (template/node [:input {:id (str "input_" id)
                                                :class "edit"
                                                :value title}])
        li (template/node [:li {:id (str "li_" id)}])]
    (listen! checkbox :change checkbox-change-handler)
    (listen! label :dblclick todo-content-handler)
    (listen! delete-link :click span-delete-click-handler)
    (listen! input-edit-todo :keypress input-edit-todo-key-press-handler)
    (listen! input-edit-todo :blur input-edit-todo-blur-handler)
    (append! div-display checkbox)
    (append! div-display label)
    (append! div-display delete-link)
    (append! li div-display)
    (append! li input-edit-todo)
    (when completed
      (set-attr! li "class" "completed")
      (set-attr! checkbox "checked" true))
    li))
 
(defn redraw-todos-ui []
  (let [new-todo (by-id "new-todo")
        todos-div (by-id "todo-list")]
    (set! (.-value new-todo) "")
    (destroy-children! todos-div)
    (.log js/console "redraw-todos-ui")
    (->> @todos
         (map redraw-todo)
         (append! todos-div))))

(defn draw-todo-count []
  (let [remaining (template/node [:span {:id "todo-count"} [:strong (str (:todo-left @stat)) (str " " (if (== 1 (:todo-left @stat)) "item" "items") " left")]])
        footer (by-id "footer")]
    (append! footer remaining)))

(defn redraw-stats-ui []
  (destroy-children! (by-id "footer"))
  (.log js/console "redraws stats ui" @stat)
  (when (< 0 (:todo-completed @stat))
    (draw-todo-clear))
  (when (:total-todo @stat)
    (draw-todo-count)))

(defn toggle-completed-todo [id completed]
  (let [todo (get-todo-by-id id)
        title (:title todo)]
    (.log js/console "toggle todo completed:" id title completed)
    (edit-todo id title completed)))

(defn change-toggle-all-checkout-state []
  (.log js/console "change toggle")
  (let [toggle-all (by-id "toggle-all")]
    (set! (.-checked toggle-all) (== (:todo-completed @stat) (count @todos)))))

(defn refresh-data []
  (.log js/console "refresh-data")
  (load-todos))

(defn add-todo [text]
  (.log js/console "add-todo" text)
  (POST "/todos" {:handler refresh-data
                  :params {:title (js/encodeURIComponent text)
                           :completed false}
                  :format :edn}))

(defn edit-todo [id text completed]
  (.log js/console "edit-todo" id text)
  (PUT (str "/todos/" id) {:handler refresh-data
                           :params {:title text
                                    :completed completed}
                           :format :edn}))

(defn span-delete-click-handler [evt]
  (let [id (.getAttribute (:target evt) "data-todo-id")]
    (.log js/console "id:::" id)
    (DELETE (str "/todos/" id) {:format :edn
                                :handler refresh-data})))

(defn new-todo-key-press-handler [evt]
  (let [key-code (:keyCode evt)]
    (when (= enter-key key-code)
      (add-todo (.-value (by-id "new-todo"))))))

(defn toggle-all-change-handler [evt]
  (.log js/console "change toggle")
  (let [checkbox (:target evt)
        checked (.-checked checkbox)
        f (fn [x] (.log js/console "val " x))]
    (doseq [todo @todos] (toggle-completed-todo  (:id todo) checked))))

(defn load-todos []
  (GET "/todos" {:format :edn :handler (fn [response]
                                          (swap! todos (fn [_] response))
                                          (compute-stats)
                                          (redraw-todos-ui)
                                          (redraw-stats-ui)
                                          (change-toggle-all-checkout-state))})
  (.log js/console "loading todos" @todos))

(defn add-event-listeners []
  (listen! (by-id "new-todo") :keypress new-todo-key-press-handler)
  (listen! (by-id "toggle-all") :change toggle-all-change-handler))

(defn window-load-handler []
  (.log js/console "window-load-handler")
  (add-event-listeners)
  (refresh-data))

(defn ^:export init []
  (.log js/console "init call")
  (window-load-handler))