(ns twitter-prime-time.core
  (:gen-class)
  (:require [environ.core :refer [env]]
            [twitter.oauth :refer [make-oauth-creds]]
            [twitter.api.restful :refer [followers-ids statuses-user-timeline]]
            [clj-time.format :as time-format]
            [clj-time.core :as t]
            [clojure.math.combinatorics :refer [cartesian-product]]
            [clojure.tools.cli :refer [parse-opts]]
            [incanter.core :as incanter]
            [incanter.charts :as charts]))

(def credentials
  "Twitter API credentials extracted from the environment"
  (let [config (:twitter-prime-time env)]
    (make-oauth-creds (:app-consumer-key config)
                      (:app-consumer-secret config)
                      (:user-access-token config)
                      (:user-access-token-secret config))))

(def date-time-format
  "Twitter's date-time formatter"
  (time-format/formatter "EEE MMM dd HH:mm:ss Z YYYY"))

(def index->day
  {1 "Monday"
   2 "Tuesday"
   3 "Wednesday"
   4 "Thursday"
   5 "Friday"
   6 "Saturday"
   7 "Sunday"})

(def cli-options
  [["-u" "--user-name USERNAME" "Twitter username for which the prime time should be found"]
   ["-m" "--maximum MAXIMUM" "Maximum number of users to download statuses from"
    :default 300
    :parse-fn #(Integer/parseInt %)
    :validate [pos?]]
   ["-h" "--help"]])

; ----- Functions -----

(defn get-followers
  "Get IDs of users following @user-name.
  FIXME: Retrieves 5000 IDs at maximum, because :next_cursor isn't followed."
  [user-name]
  (get-in (followers-ids :oauth-creds credentials
                         :params {:screen-name user-name})
          [:body :ids]))

(defn get-statuses
  "Retrieves the last 200 statuses for @user-id."
  [user-id & {:keys [sleep]
              :or {sleep 0}}]
  (when-not (zero? sleep) (Thread/sleep sleep))
  (try
    (:body (statuses-user-timeline :oauth-creds credentials
                                   :params {:user-id user-id
                                            :count 200
                                            :trim-user true}))
    (catch Exception ex ; How to better catch the exception raised when user's timeline is protected?
      [])))

(def extract-date-time
  "Extract datetime from @status and coerce them to local time zone."
  (comp #(t/to-time-zone % (t/default-time-zone))
        (partial time-format/parse date-time-format)
        :created_at))

(defn get-day-hour-frequencies
  "Get the most frequent combination of day-of-week and hour from @date-times."
  [date-times]
  (->> date-times
       (map (juxt t/day-of-week t/hour)) ; Group by the combination of day of week and hour 
       frequencies)) ; Compute frequencies of the combinations

(defn format-day-hour
  "Convert vector with @day-of-week index and @hour to a better readable text."
  [[day-of-week hour]]
  (format "%s, %d %s"
          (index->day day-of-week)
          (mod hour 12)
          (if (< hour 12) "AM" "PM")))

(defn draw-distribution
  "Draw distribution of @user-name's followers from @day-hour-frequencies.
  Return @day-hour-frequencies."
  [user-name day-hour-frequencies]
  (let [file-name (format "%s_followers_activity_%s.png"
                          user-name
                          (time-format/unparse (time-format/formatters :date) (t/now)))
        data (map (juxt identity day-hour-frequencies)
                  (cartesian-product (range 1 8) (range 0 24)))
        bar-chart (charts/bar-chart (map (comp format-day-hour first) data)  
                                    (map second data)
                                    :title (format "Twitter prime time of @%s" user-name)
                                    :x-label "Time in week"
                                    :y-label "Followers' activity")]
    (incanter/save bar-chart
                   file-name
                   :width 1000
                   :height 800)
    (println (format "Visualization of followers' activity saved into file %s." file-name))
    day-hour-frequencies))

(defn run
  "Run the whole script"
  [{:keys [user-name maximum]}]
  (let [sleep (if (> maximum 300) 3100 0)]
    (when-not (zero? sleep)
      (println (format "NOTE: Downloading data will take at least %.0f minutes."
                       (float (/ (* sleep maximum) 60000)))))
    (println "Downloading statuses...")
    (->> user-name
         get-followers
         (take maximum)
         (map (fn [follower]
                (print ".")
                (get-statuses follower :sleep sleep)))
         (apply concat)
         (map extract-date-time)
         get-day-hour-frequencies
         (draw-distribution user-name)
         (sort-by val) ; Sort by frequencies in ascending order
         last ; Take the most frequent combination
         first ; Pick the day of week-hour combination
         format-day-hour
         (format "@%s's prime time is %s." user-name)
         doall
         println)))

(defn -main
  [& args]
  (let [{{:keys [help]
          :as options} :options
         :keys [errors summary]} (parse-opts args cli-options)]
    (cond help (println summary)
          errors (println (str "Errors:\n\n" (clojure.string/join \newline errors)))
          :else (run options))))

(comment
  (def followers (get-followers "jindrichmynarz"))
  (def date-times (->> (take 10 followers)
                       (map get-statuses)
                       (apply concat)
                       (map extract-date-time)))
  (get-statuses "854550468") 
  (get-day-hour-frequencies date-times)
  (application-rate-limit-status :oauth credentials)
  (run {:user-name "jindrichmynarz" :maximum 1000})
  )
