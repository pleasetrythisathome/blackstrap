(ns om-bootstrap.docs
  (:require [cljs.core.async :as a :refer [chan put!]]
            [goog.events :as ev]
            [om-bootstrap.docs.footer :refer [footer]]
            [om-bootstrap.docs.nav :as n]
            [om-bootstrap.docs.components :refer [components-page]]
            [om-bootstrap.docs.getting-started :refer [getting-started-page]]
            [om-bootstrap.docs.home :refer [home-page]]
            [om-bootstrap.docs.shared :refer [four-oh-four]]
            [secretary.core :as route :refer-macros [defroute]]
            [weasel.repl :as ws-repl]
            [rum])
  (:require-macros [cljs.core.async.macros :refer [go-loop]])
  (:import [goog.history EventType]))

(defn shell [active-page guts]
  [:div {}
         (n/nav-main active-page)
         guts
         (footer)])

(rum/defc app
  "This is the top level component that renders the entire example
  docs page."
  [active-page]
  (shell active-page
         (case active-page
           "not-found" (four-oh-four)
           "root" (home-page)
           "getting-started" (getting-started-page)
           "components" (components-page))))

(defn load-rum [component state]
  (rum/mount (component state)
             (. js/document (getElementById "app"))))

;; ## Client Side Routing and Navigation

(defroute "/" []
  (load-rum app {:active-page "root"}))

(defroute "/getting-started" []
  (load-rum app {:active-page "getting-started"}))

(defroute "/components" []
  (load-rum app {:active-page "components"}))

(defroute "*" []
  (load-rum app {:active-page "not-found"}))

(defn listen
  "Registers a listener of type `type` on the supplied
  element. Returns a channel that contains events."
  [el type]
  (let [out (chan)]
    (ev/listen el type (fn [e] (put! out e)))
    out))

(defn setup-app
  "Sets up an event loop that listens for client side "
  []
  (let [nav (listen n/history (.-NAVIGATE EventType))]
    (go-loop []
      (let [token (.-token (a/<! nav))]
        (route/dispatch! (str "/" token)))
      (recur))))

(defn on-load
  "Loading actions for the main docs page."
  []
  (route/dispatch! (-> js/window .-location .-pathname))
  (setup-app)
  (when-not (ws-repl/alive?)
    (ws-repl/connect "ws://localhost:9001" :verbose true)))

(on-load)
