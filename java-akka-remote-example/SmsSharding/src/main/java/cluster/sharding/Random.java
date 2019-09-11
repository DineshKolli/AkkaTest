package cluster.sharding;

class Random {
    private static final java.util.Random random = new java.util.Random();

    static int whichNode = 0;

    static int node = 77;

    static CallAsEntity.CallId entityId(int from, int to) {
        //return new CallAsEntity.CallId(String.valueOf(inRange(from, to)));
        whichNode++;
        if(whichNode%3 == 0)
            return new CallAsEntity.CallId(String.valueOf(77));
        else if(whichNode%3 == 1)
            return new CallAsEntity.CallId(String.valueOf(78));
        else if(whichNode%3 == 2)
            return new CallAsEntity.CallId(String.valueOf(79));
        else
            return new CallAsEntity.CallId(String.valueOf(79));


    }

    private static int inRange(int from, int to) {
        return from + random.nextInt(to - from + 1);
    }
}