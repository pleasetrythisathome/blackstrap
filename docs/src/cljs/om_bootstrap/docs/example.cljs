(ns om-bootstrap.docs.example
  (:require [om-bootstrap.util :as u]
            [sablono.core :as html :refer-macros [html]]
            [rum]))

(defn bs-example
  ([item] [:div {:class "bs-example"} item])
  ([props item]
   [:div (u/merge-props props {:class "bs-example"})
    item]))

(defn highlight-mixin [ref]
  {:did-mount
   (fn [state]
     (let [block (.getDOMNode (:rum/react-component state) ref)]
       (.highlightBlock js/hljs block))
     state)})

(rum/defc code-block
  "Generates a component"
  [{:keys [code language]
    :or {language "clojure"}}]
  (let [code-opts (if language {:class language} {})]
    [:div
     {:class "highlight solarized-light-wrapper"}
     [:pre {:ref "highlight"}
      [:code code-opts code]]]))

(rum/defcs example
  < rum/static (rum/local {:open? false})
  [state {:keys [body code]}]
  (let [local (:rum/local state)
        {:keys [open?]} @local]
    [:div {:class "playground"}
     (bs-example body)
     (when open?
       (code-block {:code code}))
     [:a {:href "#"
          :class (u/class-set
                  {:code-toggle true
                   :open open?})
          :on-click (fn [e]
                      (swap! local update :open? not)
                      (.preventDefault e))}
      (if open?
        "hide code"
        "show code")]]))

(defn TODO []
  (example {:code "TODO" :body [:p "TODO"]}))
