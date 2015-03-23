(ns om-bootstrap.mixins
  (:require [cljs.core.async :as a :refer [put!]]
            [schema.core :as s])
  (:require-macros [schema.macros :as sm]))

;; ## Listener Mixin

(sm/defn event-listener :- (sm/=> s/Any)
  "Registers the callback on the supplied target for events of type
   `event-type`. Returns a function of no arguments that, when called,
   unregisters the callback."
  [target :- s/Any
   event-type :- s/Str
   callback :- (sm/=> s/Any s/Any)]
  (cond (.-addEventListener target)
        (do (.addEventListener target event-type callback false)
            (fn [] (.removeEventListener target event-type callback false)))

        (.-attachEvent target)
        (let [event-type (str "on" event-type)]
          (.attachEvent target event-type callback)
          (fn [] (.detachEvent target event-type callback)))
        :else (fn [])))

(def set-listener-mixin
  "Handles a sequence of listeners for the component, and removes them
   from the document when the component is unmounted."
  {:will-mount (fn [state]
                 (assoc state ::listeners (atom [])))
   :will-unmount (fn [state]
                   (update state ::listeners swap! (map #(%))))})

(defn set-listener [state target event-type callback]
  (let [remove-fn (event-listener target event-type callback)]
    (update state ::listeners swap! conj remove-fn)))

;; ## Timeout Mixin

(def set-timeout-mixin
  "Handles a sequence of timeouts for the component, and removes them
   from the document when the component is unmounted."
  {:will-mount (fn [state] (assoc state ::timeouts (atom [])))
   :will-unmount (fn [state] (update state ::timeouts swap! (partial map #(js/clearTimeout %))))})

(defn set-timeout [state f timeout]
  (let [timeout (js/setTimeout f timeout)]
    (update state ::timeouts swap! conj timeout)))

;; ## Dropdown Mixin

(defn in-root?
  "Accepts two DOM elements; returns true if the supplied node is
  nested inside the supplied root, false otherwise."
  [node root]
  (loop [node node]
    (cond (nil? node) false
          (= root node) true
          :else (recur (.-parentNode node)))))

(def ESCAPE_KEY 27)

(declare set-dropdown-state)

(defn bind-root-close-handlers!
  "For dropdowns, binds a handler for that sets the dropdown-mixin's
  `:open?` state to false if the user clicks outside the owning
  component OR hits the escape key."
  [state]
  (update state ::dropdown-listeners reset!
          [(event-listener
            js/document "click"
            (fn [e]
              (when-not (in-root? (.-target e) (.getDOMNode (:rum/react-component state)))
                (set-dropdown-state state false))))
           (event-listener
            js/document "keyup"
            (fn [e]
              (when (= ESCAPE_KEY (.-keyCode e))
                (set-dropdown-state state false))))]))

(defn unbind-root-close-handlers!
  "If they're present on the owning object, removes the listeners
  registered by the dropdown mixin."
  [state]
  (update state ::dropdown-listeners swap!
          (comp (constantly nil)
                (partial mapv #(%)))))

(def dropdown-mixin
  "Mixin that manages a single piece of state - :open?. If a user
  clicks outside the component's owning dom element OR hits the escape
  key, the state will jump back to false.

  Down the road this may need to register a callback when the state
  changes."
  {:init (fn [state props] (assoc state
                                  :open? (atom false)
                                  ::dropdown-listeners (atom [])))
   :will-unmount (fn [state] (unbind-root-close-handlers! state))})

(defn set-dropdown-state
  [state open?]
  (if open?
    (bind-root-close-handlers! state)
    (unbind-root-close-handlers! state))
  (update state :open? reset! open?)
  (rum/request-render (:rum/react-component state)))
