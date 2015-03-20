(ns om-bootstrap.rum
  (:require rum
            [plumbing.core :refer [fnk]]
            [sablono.compiler :as s]))

(defn- parse-defc [xs]
  (loop [res  {}
         xs   xs
         mode nil]
    (let [x    (first xs)
          next (next xs)]
      (cond
        (and (empty? res) (symbol? x))
          (recur {:name x} next nil)
        (vector? x) (assoc res :argvec x
                               :render next)
        (string? x) (recur (assoc res :doc x) next nil)
        (= '< x)    (recur res next :mixins)
        (= mode :mixins)
          (recur (update-in res [:mixins] (fnil conj []) x) next :mixins)
        :else
          (throw (IllegalArgumentException. (str "Syntax error at " xs)))))))

(defn- -defck [render-ctor body]
  (let [{:keys [name doc mixins argvec render]} (parse-defc body)]
   `(let [render-fn#    (fnk ~argvec ~(s/compile-html `(do ~@render)))
          render-mixin# (~render-ctor render-fn#)
          class#        (rum/build-class (concat [render-mixin#] ~mixins) ~(str name))
          ctor#         (fn [& args#]
                          (let [state# (rum/args->state args#)]
                            (rum/element class# state# nil)))]
      (def ~name ~doc (with-meta ctor# {::class class#})))))

(defmacro defck
  "Defc does couple of things:

     1. Wraps body into sablono/compile-html
     2. Generates render function from that
     3. Takes render function and mixins, builds React class from them
     4. Using that class, generates constructor fn [paramsk]->ReactElement
     5. Defines top-level var with provided name and assigns ctor to it

   Usage:

       (defc name doc-string? [< mixins+]? [paramsk] render-body+)"
  [& body]
  (-defck 'rum/render->mixin body))
