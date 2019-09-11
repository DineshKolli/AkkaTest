package cluster.sharding;

import akka.cluster.sharding.ShardRegion;

import java.io.Serializable;

class CallASMessage {
    static class ICMRequestMessage implements Serializable {
        final CallAsEntity entity;

        public ICMRequestMessage(CallAsEntity entity) {
            this.entity = entity;
        }

        @Override
        public String toString() {
            return String.format("%s %s", getClass().getSimpleName(), entity);
        }
    }

    static class ICMResponseMessage implements Serializable {
        final CallAsEntity entity;

        public ICMResponseMessage(CallAsEntity entity) {
            this.entity = entity;
        }

        @Override
        public String toString() {
            return String.format("%s %s", getClass().getSimpleName(), entity);
        }
    }

    static class CSMRequestMessage implements Serializable {
        final CallAsEntity entity;

        public CSMRequestMessage(CallAsEntity entity) {
            this.entity = entity;
        }

        @Override
        public String toString() {
            return String.format("%s %s", getClass().getSimpleName(), entity);
        }
    }

    static class CSMResponseMessage implements Serializable {
        final CallAsEntity entity;

        public CSMResponseMessage(CallAsEntity entity) {
            this.entity = entity;
        }

        @Override
        public String toString() {
            return String.format("%s %s", getClass().getSimpleName(), entity);
        }
    }

    static class ASMRequestMessage implements Serializable {
        final CallAsEntity entity;

        public ASMRequestMessage(CallAsEntity entity) {
            this.entity = entity;
        }
        @Override
        public String toString() {
            return String.format("%s", getClass().getSimpleName());
        }
    }

    static class ASMResponseMessage implements Serializable {
        final CallAsEntity entity;

        public ASMResponseMessage(CallAsEntity entity) {
            this.entity = entity;
        }
        @Override
        public String toString() {
            return String.format("%s", getClass().getSimpleName());
        }
    }

    static int count = 0;

    static ShardRegion.MessageExtractor messageExtractor() {
        final int numberOfShards = 100;

        return new ShardRegion.MessageExtractor() {
            @Override
            public String shardId(Object message) {
                String shard = extractShardIdFromCommands(message);
                return shard;
            }
            @Override
            public String entityId(Object message) {
                return extractEntityIdFromCommands(message);
            }

            @Override
            public Object entityMessage(Object message) {
                return message;
            }

            private String extractShardIdFromCommands(Object message) {
                if (message instanceof ICMRequestMessage) {
                    String shard = ((ICMRequestMessage) message).entity.id.id.hashCode() % numberOfShards + "";
                    //String shard = "dinesh".hashCode() % numberOfShards + "";
                    return shard;
                }
                else if (message instanceof CSMRequestMessage) {
                    String shard = ((CSMRequestMessage) message).entity.id.id.hashCode() % numberOfShards + "";
                    //String shard = "dinesh".hashCode() % numberOfShards + "";
                    return shard;
                }
                else if (message instanceof ASMRequestMessage) {
                    return ((ASMRequestMessage) message).entity.id.id.hashCode() % numberOfShards + "";
                }
                else if (message instanceof CSMResponseMessage) {
                    return ((CSMResponseMessage) message).entity.id.id.hashCode() % numberOfShards + "";
                }
                else if (message instanceof ICMResponseMessage) {
                    return ((ICMResponseMessage) message).entity.id.id.hashCode() % numberOfShards + "";
                }

                else {
                    return null;
                }
            }



            private String extractEntityIdFromCommands(Object message) {
                if (message instanceof ICMRequestMessage) {
                    return "icm-" + (String)((ICMRequestMessage) message).entity.id.id;
                } else if (message instanceof CSMRequestMessage) {
                    return "csm-" + (String)((CSMRequestMessage) message).entity.id.id;
                    //return ((CSMRequestMessage) message).entity.id.id;
                }
                else if (message instanceof ASMRequestMessage) {
                    return "asm-" + (String)((ASMRequestMessage) message).entity.id.id;
                }
                else if (message instanceof CSMResponseMessage) {
                    return "csm-" + (String)((CSMResponseMessage) message).entity.id.id;
                }
                else if (message instanceof ICMResponseMessage) {
                    return "icm-" + (String)((ICMResponseMessage) message).entity.id.id;
                }
                else {
                    return null;
                }
            }
        };
    }
}