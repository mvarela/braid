(ns braid.core.client.ui.views.header
  (:require-macros
   [braid.core.module-helpers :refer [defhook]])
  (:require
   [braid.core.client.helpers :refer [->color]]
   [braid.core.client.routes :as routes]
   [braid.core.client.ui.views.search-bar :refer [search-bar-view]]
   [re-frame.core :refer [subscribe]]
   [reagent.core :as r]
   [reagent.ratom :refer-macros [reaction]]
   [schema.core :as s]))

(defn loading-indicator-view [group-id]
  (let [page (subscribe [:page])]
    (fn [group-id]
      [:div.loading-indicator
       {:class (cond
                 (@page :loading?) "loading"
                 (@page :error?) "error")
        :style {:color (->color group-id)}}])))

(defn current-user-button-view []
  (let [user-id (subscribe [:user-id])
        user-avatar-url (subscribe [:user-avatar-url] [user-id])
        user-nickname (subscribe [:nickname] [user-id])
        open-group-id (subscribe [:open-group-id])
        current-path (subscribe [:page-path])]
    (fn []
      (let [path (routes/page-path {:group-id @open-group-id
                                    :page-id "me"})]
        [:a.user-info {:href path
                       :class (when (= current-path path) "active")}
          [:div.name (str "@" @user-nickname)]
          [:img.avatar {:src @user-avatar-url}]]))))

(defn group-name-view []
  (let [group (subscribe [:active-group])]
    (fn []
      [:div.group-name (@group :name)])))

(defn header-item-view
  [conf]
  (let [open-group-id (subscribe [:open-group-id])
        current-path (subscribe [:page-path])]
    ; TODO: reaction for path = current-path? Would close over conf, probably
    ; not worthwhile
    (fn [{:keys [route-fn route-args title class body]}]
      (let [path (route-fn (merge route-args {:group-id @open-group-id}))]
        [:a {:class (str class (when (= path @current-path) " active"))
             :href path
             :title title}
         body]))))

(defn group-header-buttons-view [header-items]
  [:div.buttons
   (doall
     (for [header-item header-items]
       ^{:key (header-item :title)}
       [header-item-view header-item]))])

(defn group-header-view []
  (let [group-id (subscribe [:open-group-id])]
    (fn []
      [:div.group-header
       [:div.bar {:style {:background-color (->color @group-id)}}
        [group-name-view]
        [group-header-buttons-view
         [{:title "Inbox"
           :route-fn routes/inbox-page-path
           :class "inbox"}
          {:title "Recently Closed"
           :route-fn routes/recent-page-path
           :class "recent"}
          {:title "Uploads"
           :class "group-uploads"
           :route-fn routes/uploads-path}]]
        [search-bar-view]]
       [loading-indicator-view @group-id]])))

(def admin-header-items
  [{:class "settings"
    :route-fn routes/group-settings-path
    :body "Group Settings"}
   {:class "group-bots"
    :route-fn routes/bots-path
    :body "Bots"}])

(defn admin-header-view []
  (let [user-id (subscribe [:user-id])
        open-group-id (subscribe [:open-group-id])
        admin? (subscribe [:current-user-is-group-admin?] [open-group-id])]
    (fn []
      (when @admin?
        [:div.admin-header
         [:div.admin-icon {:style {:color (->color @user-id)}}]
         [:div.options
          [:div.content
           (doall
             (for [header-item admin-header-items]
               ^{:key (header-item :class)}
               [header-item-view header-item]))]]]))))

(def user-header-items
  [{:class "subscriptions"
    :route-fn routes/page-path
    :route-args {:page-id "tags"}
    :body "Manage Subscriptions"}
   {:class "invite-friend"
    :route-fn routes/invite-page-path
    :body "Invite a Person"}
   {:class "edit-profile"
    :route-fn routes/page-path
    :route-args {:page-id "me"}
    :body "Edit Your Profile"}
   #_{:class "changelog"
    :route-fn routes/page-path
    :route-args {:page-id "changelog"}
    :body "See Changelog"}])

(defn user-header-view []
  (let [user-id (subscribe [:user-id])]
    (fn []
      [:div.user-header
       [:div.bar {:style {:background-color (->color @user-id)}}
        [current-user-button-view]
        [:div.more]]
       [:div.options
        [:div.content
         (doall
           (for [header-item user-header-items]
             ^{:key (header-item :class)}
             [header-item-view header-item]))]]])))

(defhook
  :writer register-header-view!
  :reader header-views)

(defn readonly-header-view
  []
  (let [group @(subscribe [:active-group])]
    [:div.header
     [:div.group-header
      [:div.bar {:style {:background-color (->color (group :id))}}
       [:div.group-name (group :name)]
       #_[group-header-buttons-view
        [{:title "Recently Closed"
          :route-fn routes/recent-page-path
          :class "recent"}
         {:title "Uploads"
          :class "group-uploads"
          :route-fn routes/uploads-path}]]]]
     [:div.user-header
      [:div.bar {}
       [:a.user-info {:href (routes/join-group-path {:group-id (:id group)})}
        [:div.name "Join Group"]]]]]))

(defn logged-in-header-view
  []
  (into
    [:div.header]
    (concat
      [[group-header-view]
       [:div.spacer]]
      (doall
        (for [view @header-views]
          [view]))
      [[admin-header-view]
       [user-header-view]])))

(defn header-view
  []
  (if (:readonly @(subscribe [:active-group]))
    [readonly-header-view]
    [logged-in-header-view]))
