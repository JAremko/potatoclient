// Generated by the protocol buffer compiler.  DO NOT EDIT!
// source: jon_shared_data_time.proto

// Protobuf Java Version: 3.25.1
package ser;

public final class JonSharedDataTime {
  private JonSharedDataTime() {}
  public static void registerAllExtensions(
      com.google.protobuf.ExtensionRegistryLite registry) {
  }

  public static void registerAllExtensions(
      com.google.protobuf.ExtensionRegistry registry) {
    registerAllExtensions(
        (com.google.protobuf.ExtensionRegistryLite) registry);
  }
  public interface JonGuiDataTimeOrBuilder extends
      // @@protoc_insertion_point(interface_extends:ser.JonGuiDataTime)
      com.google.protobuf.MessageOrBuilder {

    /**
     * <code>int64 timestamp = 1 [(.buf.validate.field) = { ... }</code>
     * @return The timestamp.
     */
    long getTimestamp();

    /**
     * <code>int64 manual_timestamp = 2 [(.buf.validate.field) = { ... }</code>
     * @return The manualTimestamp.
     */
    long getManualTimestamp();

    /**
     * <code>int32 zone_id = 3;</code>
     * @return The zoneId.
     */
    int getZoneId();

    /**
     * <code>bool use_manual_time = 4;</code>
     * @return The useManualTime.
     */
    boolean getUseManualTime();
  }
  /**
   * Protobuf type {@code ser.JonGuiDataTime}
   */
  public static final class JonGuiDataTime extends
      com.google.protobuf.GeneratedMessageV3 implements
      // @@protoc_insertion_point(message_implements:ser.JonGuiDataTime)
      JonGuiDataTimeOrBuilder {
  private static final long serialVersionUID = 0L;
    // Use JonGuiDataTime.newBuilder() to construct.
    private JonGuiDataTime(com.google.protobuf.GeneratedMessageV3.Builder<?> builder) {
      super(builder);
    }
    private JonGuiDataTime() {
    }

    @java.lang.Override
    @SuppressWarnings({"unused"})
    protected java.lang.Object newInstance(
        UnusedPrivateParameter unused) {
      return new JonGuiDataTime();
    }

    public static final com.google.protobuf.Descriptors.Descriptor
        getDescriptor() {
      return ser.JonSharedDataTime.internal_static_ser_JonGuiDataTime_descriptor;
    }

    @java.lang.Override
    protected com.google.protobuf.GeneratedMessageV3.FieldAccessorTable
        internalGetFieldAccessorTable() {
      return ser.JonSharedDataTime.internal_static_ser_JonGuiDataTime_fieldAccessorTable
          .ensureFieldAccessorsInitialized(
              ser.JonSharedDataTime.JonGuiDataTime.class, ser.JonSharedDataTime.JonGuiDataTime.Builder.class);
    }

    public static final int TIMESTAMP_FIELD_NUMBER = 1;
    private long timestamp_ = 0L;
    /**
     * <code>int64 timestamp = 1 [(.buf.validate.field) = { ... }</code>
     * @return The timestamp.
     */
    @java.lang.Override
    public long getTimestamp() {
      return timestamp_;
    }

    public static final int MANUAL_TIMESTAMP_FIELD_NUMBER = 2;
    private long manualTimestamp_ = 0L;
    /**
     * <code>int64 manual_timestamp = 2 [(.buf.validate.field) = { ... }</code>
     * @return The manualTimestamp.
     */
    @java.lang.Override
    public long getManualTimestamp() {
      return manualTimestamp_;
    }

    public static final int ZONE_ID_FIELD_NUMBER = 3;
    private int zoneId_ = 0;
    /**
     * <code>int32 zone_id = 3;</code>
     * @return The zoneId.
     */
    @java.lang.Override
    public int getZoneId() {
      return zoneId_;
    }

    public static final int USE_MANUAL_TIME_FIELD_NUMBER = 4;
    private boolean useManualTime_ = false;
    /**
     * <code>bool use_manual_time = 4;</code>
     * @return The useManualTime.
     */
    @java.lang.Override
    public boolean getUseManualTime() {
      return useManualTime_;
    }

    private byte memoizedIsInitialized = -1;
    @java.lang.Override
    public final boolean isInitialized() {
      byte isInitialized = memoizedIsInitialized;
      if (isInitialized == 1) return true;
      if (isInitialized == 0) return false;

      memoizedIsInitialized = 1;
      return true;
    }

    @java.lang.Override
    public void writeTo(com.google.protobuf.CodedOutputStream output)
                        throws java.io.IOException {
      if (timestamp_ != 0L) {
        output.writeInt64(1, timestamp_);
      }
      if (manualTimestamp_ != 0L) {
        output.writeInt64(2, manualTimestamp_);
      }
      if (zoneId_ != 0) {
        output.writeInt32(3, zoneId_);
      }
      if (useManualTime_ != false) {
        output.writeBool(4, useManualTime_);
      }
      getUnknownFields().writeTo(output);
    }

    @java.lang.Override
    public int getSerializedSize() {
      int size = memoizedSize;
      if (size != -1) return size;

      size = 0;
      if (timestamp_ != 0L) {
        size += com.google.protobuf.CodedOutputStream
          .computeInt64Size(1, timestamp_);
      }
      if (manualTimestamp_ != 0L) {
        size += com.google.protobuf.CodedOutputStream
          .computeInt64Size(2, manualTimestamp_);
      }
      if (zoneId_ != 0) {
        size += com.google.protobuf.CodedOutputStream
          .computeInt32Size(3, zoneId_);
      }
      if (useManualTime_ != false) {
        size += com.google.protobuf.CodedOutputStream
          .computeBoolSize(4, useManualTime_);
      }
      size += getUnknownFields().getSerializedSize();
      memoizedSize = size;
      return size;
    }

    @java.lang.Override
    public boolean equals(final java.lang.Object obj) {
      if (obj == this) {
       return true;
      }
      if (!(obj instanceof ser.JonSharedDataTime.JonGuiDataTime)) {
        return super.equals(obj);
      }
      ser.JonSharedDataTime.JonGuiDataTime other = (ser.JonSharedDataTime.JonGuiDataTime) obj;

      if (getTimestamp()
          != other.getTimestamp()) return false;
      if (getManualTimestamp()
          != other.getManualTimestamp()) return false;
      if (getZoneId()
          != other.getZoneId()) return false;
      if (getUseManualTime()
          != other.getUseManualTime()) return false;
      if (!getUnknownFields().equals(other.getUnknownFields())) return false;
      return true;
    }

    @java.lang.Override
    public int hashCode() {
      if (memoizedHashCode != 0) {
        return memoizedHashCode;
      }
      int hash = 41;
      hash = (19 * hash) + getDescriptor().hashCode();
      hash = (37 * hash) + TIMESTAMP_FIELD_NUMBER;
      hash = (53 * hash) + com.google.protobuf.Internal.hashLong(
          getTimestamp());
      hash = (37 * hash) + MANUAL_TIMESTAMP_FIELD_NUMBER;
      hash = (53 * hash) + com.google.protobuf.Internal.hashLong(
          getManualTimestamp());
      hash = (37 * hash) + ZONE_ID_FIELD_NUMBER;
      hash = (53 * hash) + getZoneId();
      hash = (37 * hash) + USE_MANUAL_TIME_FIELD_NUMBER;
      hash = (53 * hash) + com.google.protobuf.Internal.hashBoolean(
          getUseManualTime());
      hash = (29 * hash) + getUnknownFields().hashCode();
      memoizedHashCode = hash;
      return hash;
    }

    public static ser.JonSharedDataTime.JonGuiDataTime parseFrom(
        java.nio.ByteBuffer data)
        throws com.google.protobuf.InvalidProtocolBufferException {
      return PARSER.parseFrom(data);
    }
    public static ser.JonSharedDataTime.JonGuiDataTime parseFrom(
        java.nio.ByteBuffer data,
        com.google.protobuf.ExtensionRegistryLite extensionRegistry)
        throws com.google.protobuf.InvalidProtocolBufferException {
      return PARSER.parseFrom(data, extensionRegistry);
    }
    public static ser.JonSharedDataTime.JonGuiDataTime parseFrom(
        com.google.protobuf.ByteString data)
        throws com.google.protobuf.InvalidProtocolBufferException {
      return PARSER.parseFrom(data);
    }
    public static ser.JonSharedDataTime.JonGuiDataTime parseFrom(
        com.google.protobuf.ByteString data,
        com.google.protobuf.ExtensionRegistryLite extensionRegistry)
        throws com.google.protobuf.InvalidProtocolBufferException {
      return PARSER.parseFrom(data, extensionRegistry);
    }
    public static ser.JonSharedDataTime.JonGuiDataTime parseFrom(byte[] data)
        throws com.google.protobuf.InvalidProtocolBufferException {
      return PARSER.parseFrom(data);
    }
    public static ser.JonSharedDataTime.JonGuiDataTime parseFrom(
        byte[] data,
        com.google.protobuf.ExtensionRegistryLite extensionRegistry)
        throws com.google.protobuf.InvalidProtocolBufferException {
      return PARSER.parseFrom(data, extensionRegistry);
    }
    public static ser.JonSharedDataTime.JonGuiDataTime parseFrom(java.io.InputStream input)
        throws java.io.IOException {
      return com.google.protobuf.GeneratedMessageV3
          .parseWithIOException(PARSER, input);
    }
    public static ser.JonSharedDataTime.JonGuiDataTime parseFrom(
        java.io.InputStream input,
        com.google.protobuf.ExtensionRegistryLite extensionRegistry)
        throws java.io.IOException {
      return com.google.protobuf.GeneratedMessageV3
          .parseWithIOException(PARSER, input, extensionRegistry);
    }

    public static ser.JonSharedDataTime.JonGuiDataTime parseDelimitedFrom(java.io.InputStream input)
        throws java.io.IOException {
      return com.google.protobuf.GeneratedMessageV3
          .parseDelimitedWithIOException(PARSER, input);
    }

    public static ser.JonSharedDataTime.JonGuiDataTime parseDelimitedFrom(
        java.io.InputStream input,
        com.google.protobuf.ExtensionRegistryLite extensionRegistry)
        throws java.io.IOException {
      return com.google.protobuf.GeneratedMessageV3
          .parseDelimitedWithIOException(PARSER, input, extensionRegistry);
    }
    public static ser.JonSharedDataTime.JonGuiDataTime parseFrom(
        com.google.protobuf.CodedInputStream input)
        throws java.io.IOException {
      return com.google.protobuf.GeneratedMessageV3
          .parseWithIOException(PARSER, input);
    }
    public static ser.JonSharedDataTime.JonGuiDataTime parseFrom(
        com.google.protobuf.CodedInputStream input,
        com.google.protobuf.ExtensionRegistryLite extensionRegistry)
        throws java.io.IOException {
      return com.google.protobuf.GeneratedMessageV3
          .parseWithIOException(PARSER, input, extensionRegistry);
    }

    @java.lang.Override
    public Builder newBuilderForType() { return newBuilder(); }
    public static Builder newBuilder() {
      return DEFAULT_INSTANCE.toBuilder();
    }
    public static Builder newBuilder(ser.JonSharedDataTime.JonGuiDataTime prototype) {
      return DEFAULT_INSTANCE.toBuilder().mergeFrom(prototype);
    }
    @java.lang.Override
    public Builder toBuilder() {
      return this == DEFAULT_INSTANCE
          ? new Builder() : new Builder().mergeFrom(this);
    }

    @java.lang.Override
    protected Builder newBuilderForType(
        com.google.protobuf.GeneratedMessageV3.BuilderParent parent) {
      Builder builder = new Builder(parent);
      return builder;
    }
    /**
     * Protobuf type {@code ser.JonGuiDataTime}
     */
    public static final class Builder extends
        com.google.protobuf.GeneratedMessageV3.Builder<Builder> implements
        // @@protoc_insertion_point(builder_implements:ser.JonGuiDataTime)
        ser.JonSharedDataTime.JonGuiDataTimeOrBuilder {
      public static final com.google.protobuf.Descriptors.Descriptor
          getDescriptor() {
        return ser.JonSharedDataTime.internal_static_ser_JonGuiDataTime_descriptor;
      }

      @java.lang.Override
      protected com.google.protobuf.GeneratedMessageV3.FieldAccessorTable
          internalGetFieldAccessorTable() {
        return ser.JonSharedDataTime.internal_static_ser_JonGuiDataTime_fieldAccessorTable
            .ensureFieldAccessorsInitialized(
                ser.JonSharedDataTime.JonGuiDataTime.class, ser.JonSharedDataTime.JonGuiDataTime.Builder.class);
      }

      // Construct using ser.JonSharedDataTime.JonGuiDataTime.newBuilder()
      private Builder() {

      }

      private Builder(
          com.google.protobuf.GeneratedMessageV3.BuilderParent parent) {
        super(parent);

      }
      @java.lang.Override
      public Builder clear() {
        super.clear();
        bitField0_ = 0;
        timestamp_ = 0L;
        manualTimestamp_ = 0L;
        zoneId_ = 0;
        useManualTime_ = false;
        return this;
      }

      @java.lang.Override
      public com.google.protobuf.Descriptors.Descriptor
          getDescriptorForType() {
        return ser.JonSharedDataTime.internal_static_ser_JonGuiDataTime_descriptor;
      }

      @java.lang.Override
      public ser.JonSharedDataTime.JonGuiDataTime getDefaultInstanceForType() {
        return ser.JonSharedDataTime.JonGuiDataTime.getDefaultInstance();
      }

      @java.lang.Override
      public ser.JonSharedDataTime.JonGuiDataTime build() {
        ser.JonSharedDataTime.JonGuiDataTime result = buildPartial();
        if (!result.isInitialized()) {
          throw newUninitializedMessageException(result);
        }
        return result;
      }

      @java.lang.Override
      public ser.JonSharedDataTime.JonGuiDataTime buildPartial() {
        ser.JonSharedDataTime.JonGuiDataTime result = new ser.JonSharedDataTime.JonGuiDataTime(this);
        if (bitField0_ != 0) { buildPartial0(result); }
        onBuilt();
        return result;
      }

      private void buildPartial0(ser.JonSharedDataTime.JonGuiDataTime result) {
        int from_bitField0_ = bitField0_;
        if (((from_bitField0_ & 0x00000001) != 0)) {
          result.timestamp_ = timestamp_;
        }
        if (((from_bitField0_ & 0x00000002) != 0)) {
          result.manualTimestamp_ = manualTimestamp_;
        }
        if (((from_bitField0_ & 0x00000004) != 0)) {
          result.zoneId_ = zoneId_;
        }
        if (((from_bitField0_ & 0x00000008) != 0)) {
          result.useManualTime_ = useManualTime_;
        }
      }

      @java.lang.Override
      public Builder clone() {
        return super.clone();
      }
      @java.lang.Override
      public Builder setField(
          com.google.protobuf.Descriptors.FieldDescriptor field,
          java.lang.Object value) {
        return super.setField(field, value);
      }
      @java.lang.Override
      public Builder clearField(
          com.google.protobuf.Descriptors.FieldDescriptor field) {
        return super.clearField(field);
      }
      @java.lang.Override
      public Builder clearOneof(
          com.google.protobuf.Descriptors.OneofDescriptor oneof) {
        return super.clearOneof(oneof);
      }
      @java.lang.Override
      public Builder setRepeatedField(
          com.google.protobuf.Descriptors.FieldDescriptor field,
          int index, java.lang.Object value) {
        return super.setRepeatedField(field, index, value);
      }
      @java.lang.Override
      public Builder addRepeatedField(
          com.google.protobuf.Descriptors.FieldDescriptor field,
          java.lang.Object value) {
        return super.addRepeatedField(field, value);
      }
      @java.lang.Override
      public Builder mergeFrom(com.google.protobuf.Message other) {
        if (other instanceof ser.JonSharedDataTime.JonGuiDataTime) {
          return mergeFrom((ser.JonSharedDataTime.JonGuiDataTime)other);
        } else {
          super.mergeFrom(other);
          return this;
        }
      }

      public Builder mergeFrom(ser.JonSharedDataTime.JonGuiDataTime other) {
        if (other == ser.JonSharedDataTime.JonGuiDataTime.getDefaultInstance()) return this;
        if (other.getTimestamp() != 0L) {
          setTimestamp(other.getTimestamp());
        }
        if (other.getManualTimestamp() != 0L) {
          setManualTimestamp(other.getManualTimestamp());
        }
        if (other.getZoneId() != 0) {
          setZoneId(other.getZoneId());
        }
        if (other.getUseManualTime() != false) {
          setUseManualTime(other.getUseManualTime());
        }
        this.mergeUnknownFields(other.getUnknownFields());
        onChanged();
        return this;
      }

      @java.lang.Override
      public final boolean isInitialized() {
        return true;
      }

      @java.lang.Override
      public Builder mergeFrom(
          com.google.protobuf.CodedInputStream input,
          com.google.protobuf.ExtensionRegistryLite extensionRegistry)
          throws java.io.IOException {
        if (extensionRegistry == null) {
          throw new java.lang.NullPointerException();
        }
        try {
          boolean done = false;
          while (!done) {
            int tag = input.readTag();
            switch (tag) {
              case 0:
                done = true;
                break;
              case 8: {
                timestamp_ = input.readInt64();
                bitField0_ |= 0x00000001;
                break;
              } // case 8
              case 16: {
                manualTimestamp_ = input.readInt64();
                bitField0_ |= 0x00000002;
                break;
              } // case 16
              case 24: {
                zoneId_ = input.readInt32();
                bitField0_ |= 0x00000004;
                break;
              } // case 24
              case 32: {
                useManualTime_ = input.readBool();
                bitField0_ |= 0x00000008;
                break;
              } // case 32
              default: {
                if (!super.parseUnknownField(input, extensionRegistry, tag)) {
                  done = true; // was an endgroup tag
                }
                break;
              } // default:
            } // switch (tag)
          } // while (!done)
        } catch (com.google.protobuf.InvalidProtocolBufferException e) {
          throw e.unwrapIOException();
        } finally {
          onChanged();
        } // finally
        return this;
      }
      private int bitField0_;

      private long timestamp_ ;
      /**
       * <code>int64 timestamp = 1 [(.buf.validate.field) = { ... }</code>
       * @return The timestamp.
       */
      @java.lang.Override
      public long getTimestamp() {
        return timestamp_;
      }
      /**
       * <code>int64 timestamp = 1 [(.buf.validate.field) = { ... }</code>
       * @param value The timestamp to set.
       * @return This builder for chaining.
       */
      public Builder setTimestamp(long value) {

        timestamp_ = value;
        bitField0_ |= 0x00000001;
        onChanged();
        return this;
      }
      /**
       * <code>int64 timestamp = 1 [(.buf.validate.field) = { ... }</code>
       * @return This builder for chaining.
       */
      public Builder clearTimestamp() {
        bitField0_ = (bitField0_ & ~0x00000001);
        timestamp_ = 0L;
        onChanged();
        return this;
      }

      private long manualTimestamp_ ;
      /**
       * <code>int64 manual_timestamp = 2 [(.buf.validate.field) = { ... }</code>
       * @return The manualTimestamp.
       */
      @java.lang.Override
      public long getManualTimestamp() {
        return manualTimestamp_;
      }
      /**
       * <code>int64 manual_timestamp = 2 [(.buf.validate.field) = { ... }</code>
       * @param value The manualTimestamp to set.
       * @return This builder for chaining.
       */
      public Builder setManualTimestamp(long value) {

        manualTimestamp_ = value;
        bitField0_ |= 0x00000002;
        onChanged();
        return this;
      }
      /**
       * <code>int64 manual_timestamp = 2 [(.buf.validate.field) = { ... }</code>
       * @return This builder for chaining.
       */
      public Builder clearManualTimestamp() {
        bitField0_ = (bitField0_ & ~0x00000002);
        manualTimestamp_ = 0L;
        onChanged();
        return this;
      }

      private int zoneId_ ;
      /**
       * <code>int32 zone_id = 3;</code>
       * @return The zoneId.
       */
      @java.lang.Override
      public int getZoneId() {
        return zoneId_;
      }
      /**
       * <code>int32 zone_id = 3;</code>
       * @param value The zoneId to set.
       * @return This builder for chaining.
       */
      public Builder setZoneId(int value) {

        zoneId_ = value;
        bitField0_ |= 0x00000004;
        onChanged();
        return this;
      }
      /**
       * <code>int32 zone_id = 3;</code>
       * @return This builder for chaining.
       */
      public Builder clearZoneId() {
        bitField0_ = (bitField0_ & ~0x00000004);
        zoneId_ = 0;
        onChanged();
        return this;
      }

      private boolean useManualTime_ ;
      /**
       * <code>bool use_manual_time = 4;</code>
       * @return The useManualTime.
       */
      @java.lang.Override
      public boolean getUseManualTime() {
        return useManualTime_;
      }
      /**
       * <code>bool use_manual_time = 4;</code>
       * @param value The useManualTime to set.
       * @return This builder for chaining.
       */
      public Builder setUseManualTime(boolean value) {

        useManualTime_ = value;
        bitField0_ |= 0x00000008;
        onChanged();
        return this;
      }
      /**
       * <code>bool use_manual_time = 4;</code>
       * @return This builder for chaining.
       */
      public Builder clearUseManualTime() {
        bitField0_ = (bitField0_ & ~0x00000008);
        useManualTime_ = false;
        onChanged();
        return this;
      }
      @java.lang.Override
      public final Builder setUnknownFields(
          final com.google.protobuf.UnknownFieldSet unknownFields) {
        return super.setUnknownFields(unknownFields);
      }

      @java.lang.Override
      public final Builder mergeUnknownFields(
          final com.google.protobuf.UnknownFieldSet unknownFields) {
        return super.mergeUnknownFields(unknownFields);
      }


      // @@protoc_insertion_point(builder_scope:ser.JonGuiDataTime)
    }

    // @@protoc_insertion_point(class_scope:ser.JonGuiDataTime)
    private static final ser.JonSharedDataTime.JonGuiDataTime DEFAULT_INSTANCE;
    static {
      DEFAULT_INSTANCE = new ser.JonSharedDataTime.JonGuiDataTime();
    }

    public static ser.JonSharedDataTime.JonGuiDataTime getDefaultInstance() {
      return DEFAULT_INSTANCE;
    }

    private static final com.google.protobuf.Parser<JonGuiDataTime>
        PARSER = new com.google.protobuf.AbstractParser<JonGuiDataTime>() {
      @java.lang.Override
      public JonGuiDataTime parsePartialFrom(
          com.google.protobuf.CodedInputStream input,
          com.google.protobuf.ExtensionRegistryLite extensionRegistry)
          throws com.google.protobuf.InvalidProtocolBufferException {
        Builder builder = newBuilder();
        try {
          builder.mergeFrom(input, extensionRegistry);
        } catch (com.google.protobuf.InvalidProtocolBufferException e) {
          throw e.setUnfinishedMessage(builder.buildPartial());
        } catch (com.google.protobuf.UninitializedMessageException e) {
          throw e.asInvalidProtocolBufferException().setUnfinishedMessage(builder.buildPartial());
        } catch (java.io.IOException e) {
          throw new com.google.protobuf.InvalidProtocolBufferException(e)
              .setUnfinishedMessage(builder.buildPartial());
        }
        return builder.buildPartial();
      }
    };

    public static com.google.protobuf.Parser<JonGuiDataTime> parser() {
      return PARSER;
    }

    @java.lang.Override
    public com.google.protobuf.Parser<JonGuiDataTime> getParserForType() {
      return PARSER;
    }

    @java.lang.Override
    public ser.JonSharedDataTime.JonGuiDataTime getDefaultInstanceForType() {
      return DEFAULT_INSTANCE;
    }

  }

  private static final com.google.protobuf.Descriptors.Descriptor
    internal_static_ser_JonGuiDataTime_descriptor;
  private static final 
    com.google.protobuf.GeneratedMessageV3.FieldAccessorTable
      internal_static_ser_JonGuiDataTime_fieldAccessorTable;

  public static com.google.protobuf.Descriptors.FileDescriptor
      getDescriptor() {
    return descriptor;
  }
  private static  com.google.protobuf.Descriptors.FileDescriptor
      descriptor;
  static {
    java.lang.String[] descriptorData = {
      "\n\032jon_shared_data_time.proto\022\003ser\032\033buf/v" +
      "alidate/validate.proto\"y\n\016JonGuiDataTime" +
      "\022\032\n\ttimestamp\030\001 \001(\003B\007\272H\004\"\002(\000\022!\n\020manual_t" +
      "imestamp\030\002 \001(\003B\007\272H\004\"\002(\000\022\017\n\007zone_id\030\003 \001(\005" +
      "\022\027\n\017use_manual_time\030\004 \001(\010BLZJgit-codecom" +
      "mit.eu-central-1.amazonaws.com/v1/repos/" +
      "jettison/jonp/data/timeb\006proto3"
    };
    descriptor = com.google.protobuf.Descriptors.FileDescriptor
      .internalBuildGeneratedFileFrom(descriptorData,
        new com.google.protobuf.Descriptors.FileDescriptor[] {
          build.buf.validate.ValidateProto.getDescriptor(),
        });
    internal_static_ser_JonGuiDataTime_descriptor =
      getDescriptor().getMessageTypes().get(0);
    internal_static_ser_JonGuiDataTime_fieldAccessorTable = new
      com.google.protobuf.GeneratedMessageV3.FieldAccessorTable(
        internal_static_ser_JonGuiDataTime_descriptor,
        new java.lang.String[] { "Timestamp", "ManualTimestamp", "ZoneId", "UseManualTime", });
    com.google.protobuf.ExtensionRegistry registry =
        com.google.protobuf.ExtensionRegistry.newInstance();
    registry.add(build.buf.validate.ValidateProto.field);
    com.google.protobuf.Descriptors.FileDescriptor
        .internalUpdateFileDescriptor(descriptor, registry);
    build.buf.validate.ValidateProto.getDescriptor();
  }

  // @@protoc_insertion_point(outer_class_scope)
}
