/**
 * @copyright 2012 Computer Science Department, Recursive InterNetworking Architecture (RINA) laboratory, Boston University. 
 * All rights reserved. Permission to use, copy, modify, and distribute this software and its documentation
 * for any purpose and without fee is hereby granted, provided that the above copyright notice appear in all 
 * copies and that both the copyright notice and this permission notice appear in supporting documentation. 
 * The RINA laboratory of the Computer Science Department at Boston University makes no 
 * representations about the suitability of this software for any purpose. 
 * It is provided "as is" without express or implied warranty. 
 * 
 * @author Yuefeng Wang and Flavio Esposito. Computer Science Department, Boston University
 * @version 1.0 
 */
// Generated by the protocol buffer compiler.  DO NOT EDIT!
// source: IDD.proto

package rina.idd;

public final class IDD {
  private IDD() {}
  public static void registerAllExtensions(
      com.google.protobuf.ExtensionRegistry registry) {
  }
  public interface iddEntryOrBuilder extends
      com.google.protobuf.GeneratedMessage.
          ExtendableMessageOrBuilder<iddEntry> {
    
    // optional string DIFName = 1;
    boolean hasDIFName();
    String getDIFName();
    
    // optional string nmsURL = 2;
    boolean hasNmsURL();
    String getNmsURL();
    
    // repeated string ServiceURL = 3;
    java.util.List<String> getServiceURLList();
    int getServiceURLCount();
    String getServiceURL(int index);
    
    // repeated string supportingDIF = 4;
    java.util.List<String> getSupportingDIFList();
    int getSupportingDIFCount();
    String getSupportingDIF(int index);
  }
  public static final class iddEntry extends
      com.google.protobuf.GeneratedMessage.ExtendableMessage<
        iddEntry> implements iddEntryOrBuilder {
    // Use iddEntry.newBuilder() to construct.
    private iddEntry(Builder builder) {
      super(builder);
    }
    private iddEntry(boolean noInit) {}
    
    private static final iddEntry defaultInstance;
    public static iddEntry getDefaultInstance() {
      return defaultInstance;
    }
    
    public iddEntry getDefaultInstanceForType() {
      return defaultInstance;
    }
    
    public static final com.google.protobuf.Descriptors.Descriptor
        getDescriptor() {
      return rina.idd.IDD.internal_static_rina_idd_iddEntry_descriptor;
    }
    
    protected com.google.protobuf.GeneratedMessage.FieldAccessorTable
        internalGetFieldAccessorTable() {
      return rina.idd.IDD.internal_static_rina_idd_iddEntry_fieldAccessorTable;
    }
    
    private int bitField0_;
    // optional string DIFName = 1;
    public static final int DIFNAME_FIELD_NUMBER = 1;
    private java.lang.Object dIFName_;
    public boolean hasDIFName() {
      return ((bitField0_ & 0x00000001) == 0x00000001);
    }
    public String getDIFName() {
      java.lang.Object ref = dIFName_;
      if (ref instanceof String) {
        return (String) ref;
      } else {
        com.google.protobuf.ByteString bs = 
            (com.google.protobuf.ByteString) ref;
        String s = bs.toStringUtf8();
        if (com.google.protobuf.Internal.isValidUtf8(bs)) {
          dIFName_ = s;
        }
        return s;
      }
    }
    private com.google.protobuf.ByteString getDIFNameBytes() {
      java.lang.Object ref = dIFName_;
      if (ref instanceof String) {
        com.google.protobuf.ByteString b = 
            com.google.protobuf.ByteString.copyFromUtf8((String) ref);
        dIFName_ = b;
        return b;
      } else {
        return (com.google.protobuf.ByteString) ref;
      }
    }
    
    // optional string nmsURL = 2;
    public static final int NMSURL_FIELD_NUMBER = 2;
    private java.lang.Object nmsURL_;
    public boolean hasNmsURL() {
      return ((bitField0_ & 0x00000002) == 0x00000002);
    }
    public String getNmsURL() {
      java.lang.Object ref = nmsURL_;
      if (ref instanceof String) {
        return (String) ref;
      } else {
        com.google.protobuf.ByteString bs = 
            (com.google.protobuf.ByteString) ref;
        String s = bs.toStringUtf8();
        if (com.google.protobuf.Internal.isValidUtf8(bs)) {
          nmsURL_ = s;
        }
        return s;
      }
    }
    private com.google.protobuf.ByteString getNmsURLBytes() {
      java.lang.Object ref = nmsURL_;
      if (ref instanceof String) {
        com.google.protobuf.ByteString b = 
            com.google.protobuf.ByteString.copyFromUtf8((String) ref);
        nmsURL_ = b;
        return b;
      } else {
        return (com.google.protobuf.ByteString) ref;
      }
    }
    
    // repeated string ServiceURL = 3;
    public static final int SERVICEURL_FIELD_NUMBER = 3;
    private com.google.protobuf.LazyStringList serviceURL_;
    public java.util.List<String>
        getServiceURLList() {
      return serviceURL_;
    }
    public int getServiceURLCount() {
      return serviceURL_.size();
    }
    public String getServiceURL(int index) {
      return serviceURL_.get(index);
    }
    
    // repeated string supportingDIF = 4;
    public static final int SUPPORTINGDIF_FIELD_NUMBER = 4;
    private com.google.protobuf.LazyStringList supportingDIF_;
    public java.util.List<String>
        getSupportingDIFList() {
      return supportingDIF_;
    }
    public int getSupportingDIFCount() {
      return supportingDIF_.size();
    }
    public String getSupportingDIF(int index) {
      return supportingDIF_.get(index);
    }
    
    private void initFields() {
      dIFName_ = "";
      nmsURL_ = "";
      serviceURL_ = com.google.protobuf.LazyStringArrayList.EMPTY;
      supportingDIF_ = com.google.protobuf.LazyStringArrayList.EMPTY;
    }
    private byte memoizedIsInitialized = -1;
    public final boolean isInitialized() {
      byte isInitialized = memoizedIsInitialized;
      if (isInitialized != -1) return isInitialized == 1;
      
      if (!extensionsAreInitialized()) {
        memoizedIsInitialized = 0;
        return false;
      }
      memoizedIsInitialized = 1;
      return true;
    }
    
    public void writeTo(com.google.protobuf.CodedOutputStream output)
                        throws java.io.IOException {
      getSerializedSize();
      com.google.protobuf.GeneratedMessage
        .ExtendableMessage<rina.idd.IDD.iddEntry>.ExtensionWriter extensionWriter =
          newExtensionWriter();
      if (((bitField0_ & 0x00000001) == 0x00000001)) {
        output.writeBytes(1, getDIFNameBytes());
      }
      if (((bitField0_ & 0x00000002) == 0x00000002)) {
        output.writeBytes(2, getNmsURLBytes());
      }
      for (int i = 0; i < serviceURL_.size(); i++) {
        output.writeBytes(3, serviceURL_.getByteString(i));
      }
      for (int i = 0; i < supportingDIF_.size(); i++) {
        output.writeBytes(4, supportingDIF_.getByteString(i));
      }
      extensionWriter.writeUntil(536870912, output);
      getUnknownFields().writeTo(output);
    }
    
    private int memoizedSerializedSize = -1;
    public int getSerializedSize() {
      int size = memoizedSerializedSize;
      if (size != -1) return size;
    
      size = 0;
      if (((bitField0_ & 0x00000001) == 0x00000001)) {
        size += com.google.protobuf.CodedOutputStream
          .computeBytesSize(1, getDIFNameBytes());
      }
      if (((bitField0_ & 0x00000002) == 0x00000002)) {
        size += com.google.protobuf.CodedOutputStream
          .computeBytesSize(2, getNmsURLBytes());
      }
      {
        int dataSize = 0;
        for (int i = 0; i < serviceURL_.size(); i++) {
          dataSize += com.google.protobuf.CodedOutputStream
            .computeBytesSizeNoTag(serviceURL_.getByteString(i));
        }
        size += dataSize;
        size += 1 * getServiceURLList().size();
      }
      {
        int dataSize = 0;
        for (int i = 0; i < supportingDIF_.size(); i++) {
          dataSize += com.google.protobuf.CodedOutputStream
            .computeBytesSizeNoTag(supportingDIF_.getByteString(i));
        }
        size += dataSize;
        size += 1 * getSupportingDIFList().size();
      }
      size += extensionsSerializedSize();
      size += getUnknownFields().getSerializedSize();
      memoizedSerializedSize = size;
      return size;
    }
    
    private static final long serialVersionUID = 0L;
    @java.lang.Override
    protected java.lang.Object writeReplace()
        throws java.io.ObjectStreamException {
      return super.writeReplace();
    }
    
    public static rina.idd.IDD.iddEntry parseFrom(
        com.google.protobuf.ByteString data)
        throws com.google.protobuf.InvalidProtocolBufferException {
      return newBuilder().mergeFrom(data).buildParsed();
    }
    public static rina.idd.IDD.iddEntry parseFrom(
        com.google.protobuf.ByteString data,
        com.google.protobuf.ExtensionRegistryLite extensionRegistry)
        throws com.google.protobuf.InvalidProtocolBufferException {
      return newBuilder().mergeFrom(data, extensionRegistry)
               .buildParsed();
    }
    public static rina.idd.IDD.iddEntry parseFrom(byte[] data)
        throws com.google.protobuf.InvalidProtocolBufferException {
      return newBuilder().mergeFrom(data).buildParsed();
    }
    public static rina.idd.IDD.iddEntry parseFrom(
        byte[] data,
        com.google.protobuf.ExtensionRegistryLite extensionRegistry)
        throws com.google.protobuf.InvalidProtocolBufferException {
      return newBuilder().mergeFrom(data, extensionRegistry)
               .buildParsed();
    }
    public static rina.idd.IDD.iddEntry parseFrom(java.io.InputStream input)
        throws java.io.IOException {
      return newBuilder().mergeFrom(input).buildParsed();
    }
    public static rina.idd.IDD.iddEntry parseFrom(
        java.io.InputStream input,
        com.google.protobuf.ExtensionRegistryLite extensionRegistry)
        throws java.io.IOException {
      return newBuilder().mergeFrom(input, extensionRegistry)
               .buildParsed();
    }
    public static rina.idd.IDD.iddEntry parseDelimitedFrom(java.io.InputStream input)
        throws java.io.IOException {
      Builder builder = newBuilder();
      if (builder.mergeDelimitedFrom(input)) {
        return builder.buildParsed();
      } else {
        return null;
      }
    }
    public static rina.idd.IDD.iddEntry parseDelimitedFrom(
        java.io.InputStream input,
        com.google.protobuf.ExtensionRegistryLite extensionRegistry)
        throws java.io.IOException {
      Builder builder = newBuilder();
      if (builder.mergeDelimitedFrom(input, extensionRegistry)) {
        return builder.buildParsed();
      } else {
        return null;
      }
    }
    public static rina.idd.IDD.iddEntry parseFrom(
        com.google.protobuf.CodedInputStream input)
        throws java.io.IOException {
      return newBuilder().mergeFrom(input).buildParsed();
    }
    public static rina.idd.IDD.iddEntry parseFrom(
        com.google.protobuf.CodedInputStream input,
        com.google.protobuf.ExtensionRegistryLite extensionRegistry)
        throws java.io.IOException {
      return newBuilder().mergeFrom(input, extensionRegistry)
               .buildParsed();
    }
    
    public static Builder newBuilder() { return Builder.create(); }
    public Builder newBuilderForType() { return newBuilder(); }
    public static Builder newBuilder(rina.idd.IDD.iddEntry prototype) {
      return newBuilder().mergeFrom(prototype);
    }
    public Builder toBuilder() { return newBuilder(this); }
    
    @java.lang.Override
    protected Builder newBuilderForType(
        com.google.protobuf.GeneratedMessage.BuilderParent parent) {
      Builder builder = new Builder(parent);
      return builder;
    }
    public static final class Builder extends
        com.google.protobuf.GeneratedMessage.ExtendableBuilder<
          rina.idd.IDD.iddEntry, Builder> implements rina.idd.IDD.iddEntryOrBuilder {
      public static final com.google.protobuf.Descriptors.Descriptor
          getDescriptor() {
        return rina.idd.IDD.internal_static_rina_idd_iddEntry_descriptor;
      }
      
      protected com.google.protobuf.GeneratedMessage.FieldAccessorTable
          internalGetFieldAccessorTable() {
        return rina.idd.IDD.internal_static_rina_idd_iddEntry_fieldAccessorTable;
      }
      
      // Construct using rina.idd.IDD.iddEntry.newBuilder()
      private Builder() {
        maybeForceBuilderInitialization();
      }
      
      private Builder(BuilderParent parent) {
        super(parent);
        maybeForceBuilderInitialization();
      }
      private void maybeForceBuilderInitialization() {
        if (com.google.protobuf.GeneratedMessage.alwaysUseFieldBuilders) {
        }
      }
      private static Builder create() {
        return new Builder();
      }
      
      public Builder clear() {
        super.clear();
        dIFName_ = "";
        bitField0_ = (bitField0_ & ~0x00000001);
        nmsURL_ = "";
        bitField0_ = (bitField0_ & ~0x00000002);
        serviceURL_ = com.google.protobuf.LazyStringArrayList.EMPTY;
        bitField0_ = (bitField0_ & ~0x00000004);
        supportingDIF_ = com.google.protobuf.LazyStringArrayList.EMPTY;
        bitField0_ = (bitField0_ & ~0x00000008);
        return this;
      }
      
      public Builder clone() {
        return create().mergeFrom(buildPartial());
      }
      
      public com.google.protobuf.Descriptors.Descriptor
          getDescriptorForType() {
        return rina.idd.IDD.iddEntry.getDescriptor();
      }
      
      public rina.idd.IDD.iddEntry getDefaultInstanceForType() {
        return rina.idd.IDD.iddEntry.getDefaultInstance();
      }
      
      public rina.idd.IDD.iddEntry build() {
        rina.idd.IDD.iddEntry result = buildPartial();
        if (!result.isInitialized()) {
          throw newUninitializedMessageException(result);
        }
        return result;
      }
      
      private rina.idd.IDD.iddEntry buildParsed()
          throws com.google.protobuf.InvalidProtocolBufferException {
        rina.idd.IDD.iddEntry result = buildPartial();
        if (!result.isInitialized()) {
          throw newUninitializedMessageException(
            result).asInvalidProtocolBufferException();
        }
        return result;
      }
      
      public rina.idd.IDD.iddEntry buildPartial() {
        rina.idd.IDD.iddEntry result = new rina.idd.IDD.iddEntry(this);
        int from_bitField0_ = bitField0_;
        int to_bitField0_ = 0;
        if (((from_bitField0_ & 0x00000001) == 0x00000001)) {
          to_bitField0_ |= 0x00000001;
        }
        result.dIFName_ = dIFName_;
        if (((from_bitField0_ & 0x00000002) == 0x00000002)) {
          to_bitField0_ |= 0x00000002;
        }
        result.nmsURL_ = nmsURL_;
        if (((bitField0_ & 0x00000004) == 0x00000004)) {
          serviceURL_ = new com.google.protobuf.UnmodifiableLazyStringList(
              serviceURL_);
          bitField0_ = (bitField0_ & ~0x00000004);
        }
        result.serviceURL_ = serviceURL_;
        if (((bitField0_ & 0x00000008) == 0x00000008)) {
          supportingDIF_ = new com.google.protobuf.UnmodifiableLazyStringList(
              supportingDIF_);
          bitField0_ = (bitField0_ & ~0x00000008);
        }
        result.supportingDIF_ = supportingDIF_;
        result.bitField0_ = to_bitField0_;
        onBuilt();
        return result;
      }
      
      public Builder mergeFrom(com.google.protobuf.Message other) {
        if (other instanceof rina.idd.IDD.iddEntry) {
          return mergeFrom((rina.idd.IDD.iddEntry)other);
        } else {
          super.mergeFrom(other);
          return this;
        }
      }
      
      public Builder mergeFrom(rina.idd.IDD.iddEntry other) {
        if (other == rina.idd.IDD.iddEntry.getDefaultInstance()) return this;
        if (other.hasDIFName()) {
          setDIFName(other.getDIFName());
        }
        if (other.hasNmsURL()) {
          setNmsURL(other.getNmsURL());
        }
        if (!other.serviceURL_.isEmpty()) {
          if (serviceURL_.isEmpty()) {
            serviceURL_ = other.serviceURL_;
            bitField0_ = (bitField0_ & ~0x00000004);
          } else {
            ensureServiceURLIsMutable();
            serviceURL_.addAll(other.serviceURL_);
          }
          onChanged();
        }
        if (!other.supportingDIF_.isEmpty()) {
          if (supportingDIF_.isEmpty()) {
            supportingDIF_ = other.supportingDIF_;
            bitField0_ = (bitField0_ & ~0x00000008);
          } else {
            ensureSupportingDIFIsMutable();
            supportingDIF_.addAll(other.supportingDIF_);
          }
          onChanged();
        }
        this.mergeExtensionFields(other);
        this.mergeUnknownFields(other.getUnknownFields());
        return this;
      }
      
      public final boolean isInitialized() {
        if (!extensionsAreInitialized()) {
          
          return false;
        }
        return true;
      }
      
      public Builder mergeFrom(
          com.google.protobuf.CodedInputStream input,
          com.google.protobuf.ExtensionRegistryLite extensionRegistry)
          throws java.io.IOException {
        com.google.protobuf.UnknownFieldSet.Builder unknownFields =
          com.google.protobuf.UnknownFieldSet.newBuilder(
            this.getUnknownFields());
        while (true) {
          int tag = input.readTag();
          switch (tag) {
            case 0:
              this.setUnknownFields(unknownFields.build());
              onChanged();
              return this;
            default: {
              if (!parseUnknownField(input, unknownFields,
                                     extensionRegistry, tag)) {
                this.setUnknownFields(unknownFields.build());
                onChanged();
                return this;
              }
              break;
            }
            case 10: {
              bitField0_ |= 0x00000001;
              dIFName_ = input.readBytes();
              break;
            }
            case 18: {
              bitField0_ |= 0x00000002;
              nmsURL_ = input.readBytes();
              break;
            }
            case 26: {
              ensureServiceURLIsMutable();
              serviceURL_.add(input.readBytes());
              break;
            }
            case 34: {
              ensureSupportingDIFIsMutable();
              supportingDIF_.add(input.readBytes());
              break;
            }
          }
        }
      }
      
      private int bitField0_;
      
      // optional string DIFName = 1;
      private java.lang.Object dIFName_ = "";
      public boolean hasDIFName() {
        return ((bitField0_ & 0x00000001) == 0x00000001);
      }
      public String getDIFName() {
        java.lang.Object ref = dIFName_;
        if (!(ref instanceof String)) {
          String s = ((com.google.protobuf.ByteString) ref).toStringUtf8();
          dIFName_ = s;
          return s;
        } else {
          return (String) ref;
        }
      }
      public Builder setDIFName(String value) {
        if (value == null) {
    throw new NullPointerException();
  }
  bitField0_ |= 0x00000001;
        dIFName_ = value;
        onChanged();
        return this;
      }
      public Builder clearDIFName() {
        bitField0_ = (bitField0_ & ~0x00000001);
        dIFName_ = getDefaultInstance().getDIFName();
        onChanged();
        return this;
      }
      void setDIFName(com.google.protobuf.ByteString value) {
        bitField0_ |= 0x00000001;
        dIFName_ = value;
        onChanged();
      }
      
      // optional string nmsURL = 2;
      private java.lang.Object nmsURL_ = "";
      public boolean hasNmsURL() {
        return ((bitField0_ & 0x00000002) == 0x00000002);
      }
      public String getNmsURL() {
        java.lang.Object ref = nmsURL_;
        if (!(ref instanceof String)) {
          String s = ((com.google.protobuf.ByteString) ref).toStringUtf8();
          nmsURL_ = s;
          return s;
        } else {
          return (String) ref;
        }
      }
      public Builder setNmsURL(String value) {
        if (value == null) {
    throw new NullPointerException();
  }
  bitField0_ |= 0x00000002;
        nmsURL_ = value;
        onChanged();
        return this;
      }
      public Builder clearNmsURL() {
        bitField0_ = (bitField0_ & ~0x00000002);
        nmsURL_ = getDefaultInstance().getNmsURL();
        onChanged();
        return this;
      }
      void setNmsURL(com.google.protobuf.ByteString value) {
        bitField0_ |= 0x00000002;
        nmsURL_ = value;
        onChanged();
      }
      
      // repeated string ServiceURL = 3;
      private com.google.protobuf.LazyStringList serviceURL_ = com.google.protobuf.LazyStringArrayList.EMPTY;
      private void ensureServiceURLIsMutable() {
        if (!((bitField0_ & 0x00000004) == 0x00000004)) {
          serviceURL_ = new com.google.protobuf.LazyStringArrayList(serviceURL_);
          bitField0_ |= 0x00000004;
         }
      }
      public java.util.List<String>
          getServiceURLList() {
        return java.util.Collections.unmodifiableList(serviceURL_);
      }
      public int getServiceURLCount() {
        return serviceURL_.size();
      }
      public String getServiceURL(int index) {
        return serviceURL_.get(index);
      }
      public Builder setServiceURL(
          int index, String value) {
        if (value == null) {
    throw new NullPointerException();
  }
  ensureServiceURLIsMutable();
        serviceURL_.set(index, value);
        onChanged();
        return this;
      }
      public Builder addServiceURL(String value) {
        if (value == null) {
    throw new NullPointerException();
  }
  ensureServiceURLIsMutable();
        serviceURL_.add(value);
        onChanged();
        return this;
      }
      public Builder addAllServiceURL(
          java.lang.Iterable<String> values) {
        ensureServiceURLIsMutable();
        super.addAll(values, serviceURL_);
        onChanged();
        return this;
      }
      public Builder clearServiceURL() {
        serviceURL_ = com.google.protobuf.LazyStringArrayList.EMPTY;
        bitField0_ = (bitField0_ & ~0x00000004);
        onChanged();
        return this;
      }
      void addServiceURL(com.google.protobuf.ByteString value) {
        ensureServiceURLIsMutable();
        serviceURL_.add(value);
        onChanged();
      }
      
      // repeated string supportingDIF = 4;
      private com.google.protobuf.LazyStringList supportingDIF_ = com.google.protobuf.LazyStringArrayList.EMPTY;
      private void ensureSupportingDIFIsMutable() {
        if (!((bitField0_ & 0x00000008) == 0x00000008)) {
          supportingDIF_ = new com.google.protobuf.LazyStringArrayList(supportingDIF_);
          bitField0_ |= 0x00000008;
         }
      }
      public java.util.List<String>
          getSupportingDIFList() {
        return java.util.Collections.unmodifiableList(supportingDIF_);
      }
      public int getSupportingDIFCount() {
        return supportingDIF_.size();
      }
      public String getSupportingDIF(int index) {
        return supportingDIF_.get(index);
      }
      public Builder setSupportingDIF(
          int index, String value) {
        if (value == null) {
    throw new NullPointerException();
  }
  ensureSupportingDIFIsMutable();
        supportingDIF_.set(index, value);
        onChanged();
        return this;
      }
      public Builder addSupportingDIF(String value) {
        if (value == null) {
    throw new NullPointerException();
  }
  ensureSupportingDIFIsMutable();
        supportingDIF_.add(value);
        onChanged();
        return this;
      }
      public Builder addAllSupportingDIF(
          java.lang.Iterable<String> values) {
        ensureSupportingDIFIsMutable();
        super.addAll(values, supportingDIF_);
        onChanged();
        return this;
      }
      public Builder clearSupportingDIF() {
        supportingDIF_ = com.google.protobuf.LazyStringArrayList.EMPTY;
        bitField0_ = (bitField0_ & ~0x00000008);
        onChanged();
        return this;
      }
      void addSupportingDIF(com.google.protobuf.ByteString value) {
        ensureSupportingDIFIsMutable();
        supportingDIF_.add(value);
        onChanged();
      }
      
      // @@protoc_insertion_point(builder_scope:rina.idd.iddEntry)
    }
    
    static {
      defaultInstance = new iddEntry(true);
      defaultInstance.initFields();
    }
    
    // @@protoc_insertion_point(class_scope:rina.idd.iddEntry)
  }
  
  private static com.google.protobuf.Descriptors.Descriptor
    internal_static_rina_idd_iddEntry_descriptor;
  private static
    com.google.protobuf.GeneratedMessage.FieldAccessorTable
      internal_static_rina_idd_iddEntry_fieldAccessorTable;
  
  public static com.google.protobuf.Descriptors.FileDescriptor
      getDescriptor() {
    return descriptor;
  }
  private static com.google.protobuf.Descriptors.FileDescriptor
      descriptor;
  static {
    java.lang.String[] descriptorData = {
      "\n\tIDD.proto\022\010rina.idd\"`\n\010iddEntry\022\017\n\007DIF" +
      "Name\030\001 \001(\t\022\016\n\006nmsURL\030\002 \001(\t\022\022\n\nServiceURL" +
      "\030\003 \003(\t\022\025\n\rsupportingDIF\030\004 \003(\t*\010\010\005\020\200\200\200\200\002"
    };
    com.google.protobuf.Descriptors.FileDescriptor.InternalDescriptorAssigner assigner =
      new com.google.protobuf.Descriptors.FileDescriptor.InternalDescriptorAssigner() {
        public com.google.protobuf.ExtensionRegistry assignDescriptors(
            com.google.protobuf.Descriptors.FileDescriptor root) {
          descriptor = root;
          internal_static_rina_idd_iddEntry_descriptor =
            getDescriptor().getMessageTypes().get(0);
          internal_static_rina_idd_iddEntry_fieldAccessorTable = new
            com.google.protobuf.GeneratedMessage.FieldAccessorTable(
              internal_static_rina_idd_iddEntry_descriptor,
              new java.lang.String[] { "DIFName", "NmsURL", "ServiceURL", "SupportingDIF", },
              rina.idd.IDD.iddEntry.class,
              rina.idd.IDD.iddEntry.Builder.class);
          return null;
        }
      };
    com.google.protobuf.Descriptors.FileDescriptor
      .internalBuildGeneratedFileFrom(descriptorData,
        new com.google.protobuf.Descriptors.FileDescriptor[] {
        }, assigner);
  }
  
  // @@protoc_insertion_point(outer_class_scope)
}
