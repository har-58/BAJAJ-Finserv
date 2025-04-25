for (User u : users) {
    for (int fId : u.getFollows()) {
        if (userMap.containsKey(fId)) {
            User fUser = userMap.get(fId);
            if (fUser.getFollows().contains(u.getId())) {
                int min = Math.min(u.getId(), fId);
                int max = Math.max(u.getId(), fId);
                pairs.add(Arrays.asList(min, max));
            }
        }
    }
}
Set<List<Integer>> uniquePairs = new HashSet<>(pairs); // remove duplicates
