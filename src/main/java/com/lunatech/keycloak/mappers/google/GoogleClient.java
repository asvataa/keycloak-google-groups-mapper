package com.lunatech.keycloak.mappers.google;

import com.google.api.client.http.HttpRequestInitializer;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.services.directory.Directory;
import com.google.api.services.directory.DirectoryScopes;
import com.google.api.services.directory.model.Group;
import com.google.api.services.directory.model.Groups;
import com.google.auth.http.HttpCredentialsAdapter;
import com.google.auth.oauth2.GoogleCredentials;

import java.io.IOException;
import java.util.List;
import java.util.ArrayList;
import java.util.stream.Collectors;

class GoogleClient {

    private final Directory directory;

    public GoogleClient(String applicationName, String delegateUser) throws IOException {
        directory = new Directory.Builder(new NetHttpTransport(), new JacksonFactory(), credential(delegateUser))
                .setApplicationName(applicationName).build();
    }

    private static HttpRequestInitializer credential(String delegateUser) throws IOException {
        List<String> scopes = List.of(DirectoryScopes.ADMIN_DIRECTORY_GROUP_READONLY);
        GoogleCredentials credentials = GoogleCredentials.getApplicationDefault()
                .createScoped(scopes)
                .createDelegated(delegateUser);

        return new HttpCredentialsAdapter(credentials);
    }

    public List<String> getAllGroupNames(String userKey) throws IOException {
        List<String> allGroups = new ArrayList<>();
        collectGroupsRecursively(userKey, allGroups, 0, 2);
        return allGroups;
    }

    private void collectGroupsRecursively(String groupKey, List<String> allGroups, int currentDepth, int maxDepth) throws IOException {
        if (currentDepth > maxDepth) {
            return;
        }

        Groups groups = directory.groups().list().setUserKey(groupKey).execute();
        if (groups.getGroups() != null) {
            for (Group group : groups.getGroups()) {
                allGroups.add(group.getName());
                collectGroupsRecursively(group.getId(), allGroups, currentDepth + 1, maxDepth); // Увеличиваем глубину на 1
            }
        }
    }

//     public List<String> getUsergroupNames(String userKey) {
//         try {
//             System.out.println("Requesting groups for user key: " + userKey);
//             Groups groups = directory.groups().list().setUserKey(userKey).execute();
//             return groups.getGroups().stream().map(Group::getName).collect(Collectors.toList());
//         } catch(IOException e) {
//             System.err.println("Error fetching groups: " + e.getMessage());
//             throw new RuntimeException(e);
//         }
//     }

}
