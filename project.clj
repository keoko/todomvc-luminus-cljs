(defproject
  todomvc-luminus-cljs
  "0.1.0-SNAPSHOT"
  :dependencies
  [[org.clojure/clojure "1.5.1"]
   [lib-noir "0.5.0"]
   [compojure "1.1.5"]
   [ring-server "0.2.7"]
   [clabango "0.5"]
   [com.taoensso/timbre "1.5.2"]
   [com.taoensso/tower "1.5.1"]
   [markdown-clj "0.9.19"]
   [org.clojure/java.jdbc "0.2.3"]
   [com.h2database/h2 "1.3.170"]
   [korma "0.3.0-RC5"]
   [log4j
    "1.2.15"
    :exclusions
    [javax.mail/mail
     javax.jms/jms
     com.sun.jdmk/jmxtools
     com.sun.jmx/jmxri]]
   [org.clojure/clojurescript "0.0-1586"]
   [domina "1.0.1"]
   [prismatic/dommy "0.1.0"]
   [cljs-ajax "0.1.0"]]
  :cljsbuild
  {:builds
   [{:source-paths ["src-cljs"],
     :compiler
     {:pretty-print true,
      :output-to "resources/public/js/site.js",
      ;;      :optimizations :advanced
      }}]}
  :ring
  {:handler todomvc-luminus-cljs.handler/war-handler,
   :init todomvc-luminus-cljs.handler/init,
   :destroy todomvc-luminus-cljs.handler/destroy}
  :profiles
  {:production
   {:ring
    {:open-browser? false, :stacktraces? false, :auto-reload? false}},
   :dev
   {:dependencies [[ring-mock "0.1.3"] [ring/ring-devel "1.1.8"]]}}
  :url
  "http://example.com/FIXME"
  :plugins
  [[lein-ring "0.8.3"] [lein-cljsbuild "0.3.0"]]
  :description
  "FIXME: write description"
  :min-lein-version "2.0.0")