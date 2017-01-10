(ns clj-vlc.utils
  (:require [clj-tcp.client :as tcp]))

(defn get-vlc-return
  ;; return string result of a VLC command
  [channel]
  (if (tcp/open? channel)
    (loop [acc ""]
      (let [islast? (re-find #">|Shutting down." acc)]
        (if islast?
          acc
          (recur (str acc (clojure.string/replace (String. (tcp/read! channel)) #"\n|\r" ""))))))
    nil)) ;; FIXME treat errors better

(defn send-vlc-command
  ;; send vlc command on a channel, returns a string of VLC message 
  [channel command]
  (if (tcp/open? channel)
    (let [_ (tcp/write! channel (.getBytes (str command "\n")))] 
     (get-vlc-return channel))
    nil)) ; FIXME treat errors better


(defn run-vlc-cmds-tcp
  ;; run vlc cmds (e.g., :add "yourfile" :play :pause) through the channel,
  ;; returns VLC replies as string list
  [channel & cmds]
  (let [cs (reduce 
               (fn [acc x] (if (keyword? x) (conj acc [x]) (update-in acc [(dec (count acc))] #(conj % x) ))) 
               []
               cmds)
        r (map (fn [x] (send-vlc-command channel (clojure.string/join " " (map name x)))) cs)]
    r))

(defn get-channel 
  ;; returns channel associated to port
  [port]
  (let [c (tcp/client "localhost" port {:reuse-client true})
        _ (get-vlc-return c)]
   c))
