(ns clj-vlc.core-test
  (:require [clojure.test :refer :all]
            [clj-vlc.core :refer :all]
            [clj-tcp.client :as tcp]))

(def port (get-free-port))

(def video (String. (.getPath (clojure.java.io/resource "video-by-pexels.mp4"))))

(deftest test-start-vlc-server
  (testing "We obtain an open channel to work with VLC"
    (let [c (start-vlc-server port)]
    (is (tcp/open? c))
    (end-vlc-server c))))

(deftest test-add-play-command
  (testing "We add a video and play it"
    (let [c (start-vlc-server port)]
         (is (= (list "> " "> ") (run-vlc-cmds c :add video :play)))
         (Thread/sleep 100) ; VLC takes a bit of time to run the
                            ; video, we do not want to close the
                            ; connection to fast
         (end-vlc-server c))))

(deftest test-invalid-command
  (testing "We use an invalid command and we should get an exception"
    (let [c (start-vlc-server port)]
         (is (thrown-with-msg? Exception #"Input commands are invalid!" (run-vlc-cmds c :playnow "Now I said!")))
         (Thread/sleep 100) ; VLC takes a bit of time to run the
                            ; video, we do not want to close the
                            ; connection too fast
         (end-vlc-server c))))

(deftest test-end-vlc-server
  (testing "The channel is closed when we stop to work with VLC"
    (is (= true (end-vlc-server (start-vlc-server port))))))
