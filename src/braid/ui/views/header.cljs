(ns braid.ui.views.header
  (:require [chat.client.dispatcher :refer [dispatch!]]
            [chat.client.routes :as routes]
            [chat.client.views.helpers :refer [id->color]]
            [chat.client.reagent-adapter :refer [subscribe]]
            [braid.ui.views.pills :refer [tag-pill-view user-pill-view]]
            [braid.ui.views.search-bar :refer [search-bar-view]]))

(defn clear-inbox-button-view []
  (let [group-id (subscribe [:open-group-id])
        open-threads (subscribe [:open-threads] [group-id])]
    (fn []
      [:div.clear-inbox
        (when (< 5 (count @open-threads))
          [:button {:on-click (fn [_]
                                (dispatch! :clear-inbox))}
            "Clear Inbox"])])))

(defn users-online-pane-view []
  (let [open-group-id (subscribe [:open-group-id])
        user-id (subscribe [:user-id])
        users (subscribe [:users-in-open-group :online])]
    (fn []
      (let [users (->> @users
                       (remove (fn [user]
                                   (= @user-id
                                      (user :id)))))
            path (routes/users-page-path {:group-id @open-group-id})]
        [:div.users.shortcut {:class (when (routes/current-path? path) "active")}
          [:a.title {:href path
                     :title "Users"}
                  (count users)]
                 [:div.modal
                  [:h2 "Online"]
                  (for [user users]
                    ^{:key (user :id)}
                    [user-pill-view (user :id)])]]))))





#_(defn header-view []
  (let [group-id (subscribe [:open-group-id])
        admin? (subscribe [:current-user-is-group-admin?] [group-id])]
    (fn []
    [:div.header
     [clear-inbox-button-view]
     [inbox-page-button-view]
     [recent-page-button-view]
     [users-online-pane-view]
     [tags-pane-view]
     [group-settings-view]
     [search-bar-view]
     [current-user-button-view]])))

(defn inbox-page-button-view []
  (let [open-group-id (subscribe [:open-group-id])]
    (fn []
      (let [path (routes/inbox-page-path {:group-id @open-group-id})]
        [:a.inbox {:class (when (routes/current-path? path) "active")
                   :href path
                   :title "Inbox"}]))))

(defn recent-page-button-view []
  (let [open-group-id (subscribe [:open-group-id])]
    (fn []
      (let [path (routes/recent-page-path {:group-id @open-group-id})]
        [:a.recent {:class (when (routes/current-path? path) "active")
                    :href path
                    :title "Recent"}]))))

(defn current-user-button-view []
  (let [user-id (subscribe [:user-id])
        user-avatar-url (subscribe [:user-avatar-url @user-id])
        user-nickname (subscribe [:nickname @user-id])
        open-group-id (subscribe [:open-group-id])]
    (fn []
      (let [path (routes/page-path {:group-id @open-group-id
                                    :page-id "me"})]
        [:a.user-info {:href path
                       :class (when (routes/current-path? path) "active")}
          [:div.name @user-nickname]
          [:img.avatar {:style {:background-color (id->color @user-id)}
                        :src @user-avatar-url}]]))))

(defn group-name-view []
  (let [group (subscribe [:active-group])]
    (fn []
      [:div.group-name (@group :name)])))

(defn subscriptions-link-view []
  (let [group-id (subscribe [:open-group-id])]
    (fn []
      (let [path (routes/page-path {:group-id @group-id
                                    :page-id "tags"})]
        [:a.subscriptions
         {:class (when (routes/current-path? path) "active")
          :href path}
         "Manage Subscriptions"]))))

(defn settings-link-view []
  (let [group-id (subscribe [:open-group-id])]
    (fn []
      (let [path (routes/group-settings-path {:group-id @group-id})]
        [:a.settings
         {:class (when (routes/current-path? path) "active")
          :href path}
         "Settings"]))))

(defn header-view []
  [:div.header

    [:div.left
      [group-name-view]
      [inbox-page-button-view]
      [recent-page-button-view]
      [search-bar-view]]

    [:div.right
      [:div.bar
        [current-user-button-view]
        [:div.more]]
      [:div.options
        [subscriptions-link-view]
        [:a.invite-friend {:href ""} "Invite a Friend"]
        [:a.edit-profile {:href ""} "Edit Your Profile"]
        [settings-link-view]]]])
