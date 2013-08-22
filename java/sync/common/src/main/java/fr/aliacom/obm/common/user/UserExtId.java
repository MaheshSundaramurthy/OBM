package fr.aliacom.obm.common.user;

import com.google.common.base.Objects;

public class UserExtId {

	private final String extId;

	public UserExtId(String extId) {
		this.extId = extId;
	}

	public String getExtId() {
		return extId;
	}

	public String serializeToString() {
		return extId;
	}

	@Override
	public final boolean equals(Object obj) {
		if (obj instanceof UserExtId) {
			UserExtId other = (UserExtId) obj;
			return Objects.equal(extId, other.extId);
		}
		return false;
	}

	@Override
	public final int hashCode() {
		return Objects.hashCode(extId);
	}

	@Override
	public String toString() {
		return Objects.toStringHelper(this).add("extId", extId).toString();
	}

}
