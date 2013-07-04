/**
 * Autogenerated by Thrift Compiler (1.0.0-dev)
 *
 * DO NOT EDIT UNLESS YOU ARE SURE THAT YOU KNOW WHAT YOU ARE DOING
 *  @generated
 */
package streamcorpus;

import org.apache.thrift.scheme.IScheme;
import org.apache.thrift.scheme.SchemeFactory;
import org.apache.thrift.scheme.StandardScheme;

import org.apache.thrift.scheme.TupleScheme;
import org.apache.thrift.protocol.TTupleProtocol;
import java.util.Map;
import java.util.HashMap;
import java.util.EnumMap;
import java.util.EnumSet;
import java.util.Collections;
import java.util.BitSet;

/**
 * Description of a natural language used in text
 */
public class Language implements org.apache.thrift.TBase<Language, Language._Fields>, java.io.Serializable, Cloneable,
		Comparable<Language> {
	private static final org.apache.thrift.protocol.TStruct						STRUCT_DESC			= new org.apache.thrift.protocol.TStruct(
																																												"Language");

	private static final org.apache.thrift.protocol.TField						CODE_FIELD_DESC	= new org.apache.thrift.protocol.TField(
																																												"code",
																																												org.apache.thrift.protocol.TType.STRING,
																																												(short) 1);
	private static final org.apache.thrift.protocol.TField						NAME_FIELD_DESC	= new org.apache.thrift.protocol.TField(
																																												"name",
																																												org.apache.thrift.protocol.TType.STRING,
																																												(short) 2);

	private static final Map<Class<? extends IScheme>, SchemeFactory>	schemes					= new HashMap<Class<? extends IScheme>, SchemeFactory>();
	static {
		schemes.put(StandardScheme.class, new LanguageStandardSchemeFactory());
		schemes.put(TupleScheme.class, new LanguageTupleSchemeFactory());
	}

	/**
	 * two letter code for the language
	 */
	public String																											code;																																		// required
	public String																											name;																																		// optional

	/**
	 * The set of fields this struct contains, along with convenience methods for finding and
	 * manipulating them.
	 */
	public enum _Fields implements org.apache.thrift.TFieldIdEnum {
		/**
		 * two letter code for the language
		 */
		CODE((short) 1, "code"), NAME((short) 2, "name");

		private static final Map<String, _Fields>	byName	= new HashMap<String, _Fields>();

		static {
			for (_Fields field : EnumSet.allOf(_Fields.class)) {
				byName.put(field.getFieldName(), field);
			}
		}

		/**
		 * Find the _Fields constant that matches fieldId, or null if its not found.
		 */
		public static _Fields findByThriftId(int fieldId) {
			switch (fieldId) {
			case 1: // CODE
				return CODE;
			case 2: // NAME
				return NAME;
			default:
				return null;
			}
		}

		/**
		 * Find the _Fields constant that matches fieldId, throwing an exception if it is not found.
		 */
		public static _Fields findByThriftIdOrThrow(int fieldId) {
			_Fields fields = findByThriftId(fieldId);
			if (fields == null)
				throw new IllegalArgumentException("Field " + fieldId + " doesn't exist!");
			return fields;
		}

		/**
		 * Find the _Fields constant that matches name, or null if its not found.
		 */
		public static _Fields findByName(String name) {
			return byName.get(name);
		}

		private final short		_thriftId;
		private final String	_fieldName;

		_Fields(short thriftId, String fieldName) {
			_thriftId = thriftId;
			_fieldName = fieldName;
		}

		@Override
		public short getThriftFieldId() {
			return _thriftId;
		}

		@Override
		public String getFieldName() {
			return _fieldName;
		}
	}

	// isset id assignments
	private _Fields																															optionals[]	= { _Fields.NAME };
	public static final Map<_Fields, org.apache.thrift.meta_data.FieldMetaData>	metaDataMap;
	static {
		Map<_Fields, org.apache.thrift.meta_data.FieldMetaData> tmpMap = new EnumMap<_Fields, org.apache.thrift.meta_data.FieldMetaData>(
				_Fields.class);
		tmpMap.put(_Fields.CODE, new org.apache.thrift.meta_data.FieldMetaData("code",
				org.apache.thrift.TFieldRequirementType.DEFAULT, new org.apache.thrift.meta_data.FieldValueMetaData(
						org.apache.thrift.protocol.TType.STRING)));
		tmpMap.put(_Fields.NAME, new org.apache.thrift.meta_data.FieldMetaData("name",
				org.apache.thrift.TFieldRequirementType.OPTIONAL, new org.apache.thrift.meta_data.FieldValueMetaData(
						org.apache.thrift.protocol.TType.STRING)));
		metaDataMap = Collections.unmodifiableMap(tmpMap);
		org.apache.thrift.meta_data.FieldMetaData.addStructMetaDataMap(Language.class, metaDataMap);
	}

	public Language() {
	}

	public Language(String code) {
		this();
		this.code = code;
	}

	/**
	 * Performs a deep copy on <i>other</i>.
	 */
	public Language(Language other) {
		if (other.isSetCode()) {
			this.code = other.code;
		}
		if (other.isSetName()) {
			this.name = other.name;
		}
	}

	@Override
	public Language deepCopy() {
		return new Language(this);
	}

	@Override
	public void clear() {
		this.code = null;
		this.name = null;
	}

	/**
	 * two letter code for the language
	 */
	public String getCode() {
		return this.code;
	}

	/**
	 * two letter code for the language
	 */
	public Language setCode(String code) {
		this.code = code;
		return this;
	}

	public void unsetCode() {
		this.code = null;
	}

	/** Returns true if field code is set (has been assigned a value) and false otherwise */
	public boolean isSetCode() {
		return this.code != null;
	}

	public void setCodeIsSet(boolean value) {
		if (!value) {
			this.code = null;
		}
	}

	public String getName() {
		return this.name;
	}

	public Language setName(String name) {
		this.name = name;
		return this;
	}

	public void unsetName() {
		this.name = null;
	}

	/** Returns true if field name is set (has been assigned a value) and false otherwise */
	public boolean isSetName() {
		return this.name != null;
	}

	public void setNameIsSet(boolean value) {
		if (!value) {
			this.name = null;
		}
	}

	@Override
	public void setFieldValue(_Fields field, Object value) {
		switch (field) {
		case CODE:
			if (value == null) {
				unsetCode();
			} else {
				setCode((String) value);
			}
			break;

		case NAME:
			if (value == null) {
				unsetName();
			} else {
				setName((String) value);
			}
			break;

		}
	}

	@Override
	public Object getFieldValue(_Fields field) {
		switch (field) {
		case CODE:
			return getCode();

		case NAME:
			return getName();

		}
		throw new IllegalStateException();
	}

	/**
	 * Returns true if field corresponding to fieldID is set (has been assigned a value) and false
	 * otherwise
	 */
	@Override
	public boolean isSet(_Fields field) {
		if (field == null) {
			throw new IllegalArgumentException();
		}

		switch (field) {
		case CODE:
			return isSetCode();
		case NAME:
			return isSetName();
		}
		throw new IllegalStateException();
	}

	@Override
	public boolean equals(Object that) {
		if (that == null)
			return false;
		if (that instanceof Language)
			return this.equals((Language) that);
		return false;
	}

	public boolean equals(Language that) {
		if (that == null)
			return false;

		boolean this_present_code = true && this.isSetCode();
		boolean that_present_code = true && that.isSetCode();
		if (this_present_code || that_present_code) {
			if (!(this_present_code && that_present_code))
				return false;
			if (!this.code.equals(that.code))
				return false;
		}

		boolean this_present_name = true && this.isSetName();
		boolean that_present_name = true && that.isSetName();
		if (this_present_name || that_present_name) {
			if (!(this_present_name && that_present_name))
				return false;
			if (!this.name.equals(that.name))
				return false;
		}

		return true;
	}

	@Override
	public int hashCode() {
		return 0;
	}

	@Override
	public int compareTo(Language other) {
		if (!getClass().equals(other.getClass())) {
			return getClass().getName().compareTo(other.getClass().getName());
		}

		int lastComparison = 0;

		lastComparison = Boolean.valueOf(isSetCode()).compareTo(other.isSetCode());
		if (lastComparison != 0) {
			return lastComparison;
		}
		if (isSetCode()) {
			lastComparison = org.apache.thrift.TBaseHelper.compareTo(this.code, other.code);
			if (lastComparison != 0) {
				return lastComparison;
			}
		}
		lastComparison = Boolean.valueOf(isSetName()).compareTo(other.isSetName());
		if (lastComparison != 0) {
			return lastComparison;
		}
		if (isSetName()) {
			lastComparison = org.apache.thrift.TBaseHelper.compareTo(this.name, other.name);
			if (lastComparison != 0) {
				return lastComparison;
			}
		}
		return 0;
	}

	@Override
	public _Fields fieldForId(int fieldId) {
		return _Fields.findByThriftId(fieldId);
	}

	@Override
	public void read(org.apache.thrift.protocol.TProtocol iprot) throws org.apache.thrift.TException {
		schemes.get(iprot.getScheme()).getScheme().read(iprot, this);
	}

	@Override
	public void write(org.apache.thrift.protocol.TProtocol oprot) throws org.apache.thrift.TException {
		schemes.get(oprot.getScheme()).getScheme().write(oprot, this);
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder("Language(");
		boolean first = true;

		sb.append("code:");
		if (this.code == null) {
			sb.append("null");
		} else {
			sb.append(this.code);
		}
		first = false;
		if (isSetName()) {
			if (!first)
				sb.append(", ");
			sb.append("name:");
			if (this.name == null) {
				sb.append("null");
			} else {
				sb.append(this.name);
			}
			first = false;
		}
		sb.append(")");
		return sb.toString();
	}

	public void validate() throws org.apache.thrift.TException {
		// check for required fields
		// check for sub-struct validity
	}

	private void writeObject(java.io.ObjectOutputStream out) throws java.io.IOException {
		try {
			write(new org.apache.thrift.protocol.TCompactProtocol(new org.apache.thrift.transport.TIOStreamTransport(out)));
		} catch (org.apache.thrift.TException te) {
			throw new java.io.IOException(te);
		}
	}

	private void readObject(java.io.ObjectInputStream in) throws java.io.IOException, ClassNotFoundException {
		try {
			read(new org.apache.thrift.protocol.TCompactProtocol(new org.apache.thrift.transport.TIOStreamTransport(in)));
		} catch (org.apache.thrift.TException te) {
			throw new java.io.IOException(te);
		}
	}

	private static class LanguageStandardSchemeFactory implements SchemeFactory {
		@Override
		public LanguageStandardScheme getScheme() {
			return new LanguageStandardScheme();
		}
	}

	private static class LanguageStandardScheme extends StandardScheme<Language> {

		@Override
		public void read(org.apache.thrift.protocol.TProtocol iprot, Language struct) throws org.apache.thrift.TException {
			org.apache.thrift.protocol.TField schemeField;
			iprot.readStructBegin();
			while (true) {
				schemeField = iprot.readFieldBegin();
				if (schemeField.type == org.apache.thrift.protocol.TType.STOP) {
					break;
				}
				switch (schemeField.id) {
				case 1: // CODE
					if (schemeField.type == org.apache.thrift.protocol.TType.STRING) {
						struct.code = iprot.readString();
						struct.setCodeIsSet(true);
					} else {
						org.apache.thrift.protocol.TProtocolUtil.skip(iprot, schemeField.type);
					}
					break;
				case 2: // NAME
					if (schemeField.type == org.apache.thrift.protocol.TType.STRING) {
						struct.name = iprot.readString();
						struct.setNameIsSet(true);
					} else {
						org.apache.thrift.protocol.TProtocolUtil.skip(iprot, schemeField.type);
					}
					break;
				default:
					org.apache.thrift.protocol.TProtocolUtil.skip(iprot, schemeField.type);
				}
				iprot.readFieldEnd();
			}
			iprot.readStructEnd();

			// check for required fields of primitive type, which can't be checked in the validate method
			struct.validate();
		}

		@Override
		public void write(org.apache.thrift.protocol.TProtocol oprot, Language struct) throws org.apache.thrift.TException {
			struct.validate();

			oprot.writeStructBegin(STRUCT_DESC);
			if (struct.code != null) {
				oprot.writeFieldBegin(CODE_FIELD_DESC);
				oprot.writeString(struct.code);
				oprot.writeFieldEnd();
			}
			if (struct.name != null) {
				if (struct.isSetName()) {
					oprot.writeFieldBegin(NAME_FIELD_DESC);
					oprot.writeString(struct.name);
					oprot.writeFieldEnd();
				}
			}
			oprot.writeFieldStop();
			oprot.writeStructEnd();
		}

	}

	private static class LanguageTupleSchemeFactory implements SchemeFactory {
		@Override
		public LanguageTupleScheme getScheme() {
			return new LanguageTupleScheme();
		}
	}

	private static class LanguageTupleScheme extends TupleScheme<Language> {

		@Override
		public void write(org.apache.thrift.protocol.TProtocol prot, Language struct) throws org.apache.thrift.TException {
			TTupleProtocol oprot = (TTupleProtocol) prot;
			BitSet optionals = new BitSet();
			if (struct.isSetCode()) {
				optionals.set(0);
			}
			if (struct.isSetName()) {
				optionals.set(1);
			}
			oprot.writeBitSet(optionals, 2);
			if (struct.isSetCode()) {
				oprot.writeString(struct.code);
			}
			if (struct.isSetName()) {
				oprot.writeString(struct.name);
			}
		}

		@Override
		public void read(org.apache.thrift.protocol.TProtocol prot, Language struct) throws org.apache.thrift.TException {
			TTupleProtocol iprot = (TTupleProtocol) prot;
			BitSet incoming = iprot.readBitSet(2);
			if (incoming.get(0)) {
				struct.code = iprot.readString();
				struct.setCodeIsSet(true);
			}
			if (incoming.get(1)) {
				struct.name = iprot.readString();
				struct.setNameIsSet(true);
			}
		}
	}

}
