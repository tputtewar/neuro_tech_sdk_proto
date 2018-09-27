package com.neuro.app.util;

public enum Roles {

	ADMIN, // This role has the most capabilities.
	POWER, // This role can edit all shared objects and alerts, tag events, and other
			// similar tasks.
	USER, // This role can create and edit its own saved searches, run searches, edit
			// preferences, create and edit event types, and other similar tasks.
	CAN_DELETE, // This role has the single capability of deleting by keyword, which is required
				// for using the delete search operator.
	SPLUNK_SYSTEM_ROLE;// This role is based on admin, but has more restrictions on searches and jobs.

}
