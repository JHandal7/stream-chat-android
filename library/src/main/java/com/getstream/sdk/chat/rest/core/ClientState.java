package com.getstream.sdk.chat.rest.core;

import com.getstream.sdk.chat.model.Channel;
import com.getstream.sdk.chat.model.Member;
import com.getstream.sdk.chat.model.Watcher;
import com.getstream.sdk.chat.rest.User;
import com.getstream.sdk.chat.rest.response.ChannelState;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Client state is responsible for storing:
 * <p>
 * - current user
 * - all users that the APIs returned
 * - the unread count for the current user
 * - the mutes for the current user
 * <p>
 * This means that the following API calls should update client state
 * <p>
 * - queryUsers
 * - queryChannels
 * - queryChannel
 * <p>
 * And we need to store mapping between channel id -> user for efficient updating of users
 * <p>
 * To keep this data in sync it needs to monitor the following events
 * <p>
 * - mute event (in case you mute a new user) (done)
 * - user.updated in case some of the current users have changed (done)
 * - mark read (for the unread count) (done)
 * - health.check for the initial user data (done)
 * - user.presence.changed for when a user went online/offline (done)
 * - new message (done)
 * - channel.updated (members could change) (done)
 * - new channel notification (done)
 * <p>
 * When a user did update (either via update, or a presence change)
 * - we need to see which channels contain that user (members or channels)
 * - update the user object on the members and channels
 */
public class ClientState {


    private static final String TAG = ClientState.class.getSimpleName();
    private ConcurrentHashMap<String, User> users = new ConcurrentHashMap<>();
    private User currentUser;
    private Integer currentUserUnreadCount;
    private ConcurrentHashMap<String, List<String>> userIDToChannelsMap;
    private Client client;

    /**
     * Creates the client state, only required paramter is a client object
     *
     * @param client
     */
    public ClientState(Client client) {
        this.client = client;
        currentUser = null;
        currentUserUnreadCount = null;
        userIDToChannelsMap = new ConcurrentHashMap<>();
        users = new ConcurrentHashMap<>();
    }


    /**
     * Returns the current user
     *
     * @return
     */
    public User getCurrentUser() {
        return currentUser;
    }

    void setCurrentUser(User currentUser) {
        this.currentUser = currentUser;
    }

    /**
     * Get a user by ID
     *
     * @param userID a string based ID for the user
     * @return
     */
    public User getUser(String userID) {
        return users.get(userID);
    }

    /**
     * Returns the unread count for the current user
     *
     * @return
     */
    public Integer getCurrentUserUnreadCount() {
        return currentUserUnreadCount;
    }

    void setCurrentUserUnreadCount(Integer currentUserUnreadCount) {
        this.currentUserUnreadCount = currentUserUnreadCount;
    }

    void updateUser(User newUser) {
        List<User> newUsers = new ArrayList();
        newUsers.add(newUser);
        updateUsers(newUsers);
    }

    void updateUsers(List<User> newUsers) {
        Map<String, Channel> channelMap = client.getActiveChannelMap();

        for (User newUser : newUsers) {
            User oldUser = users.get(newUser.getId());
            if (oldUser != null) {
                oldUser.shallowUpdate(newUser);
            } else {
                users.put(newUser.getId(), newUser.shallowCopy());
            }

            // if there are existing references, update them
            List<String> cids = userIDToChannelsMap.get(newUser.getId());
            if (cids != null) {
                for (String cid : cids) {
                    Channel channel = channelMap.get(cid);
                    if (channel != null) {
                        // update the members
                        for (Member m : channel.getChannelState().getMembers()) {
                            if (m.getUserId().equals(newUser.getId())) {
                                m.getUser().shallowUpdate(newUser);
                            }
                        }
                        // update the watchers
                        // TODO: inefficient if you have many at the same time
                        for (Watcher w : channel.getChannelState().getWatchers()) {
                            if (w.getUserId().equals(newUser.getId())) {
                                w.getUser().shallowUpdate(newUser);
                            }
                        }
                    }

                }
            }
        }


    }

    void updateUserWithReference(User newUser, String cid) {
        List<User> newUsers = new ArrayList();
        newUsers.add(newUser);
        updateUsersWithReference(newUsers, cid);
    }

    public void updateUsersForChannel(ChannelState channelState) {
        List<User> users = new ArrayList<>();
        for (Member m : channelState.getMembers()) {
            users.add(m.getUser());
        }
        for (Watcher w : channelState.getWatchers()) {
            users.add(w.getUser());
        }
        updateUsersWithReference(users, channelState.getChannel().getCid());
    }

    void updateUsersWithReference(List<User> newUsers, String cid) {
        // update the users first
        updateUsers(newUsers);

        // set the references
        for (User newUser : newUsers) {
            List<String> channelRefs = userIDToChannelsMap.get(newUser.getId());
            if (channelRefs == null) {
                channelRefs = new ArrayList<>();
            }
            channelRefs.add(cid);
        }
    }
}
