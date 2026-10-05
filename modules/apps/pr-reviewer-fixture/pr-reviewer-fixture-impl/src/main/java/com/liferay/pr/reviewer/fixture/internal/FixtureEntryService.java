/**
 * SPDX-FileCopyrightText: (c) 2000 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.pr.reviewer.fixture.internal;

import com.liferay.petra.function.transform.TransformUtil;
import com.liferay.petra.string.StringBundler;
import com.liferay.portal.kernel.log.Log;
import com.liferay.portal.kernel.log.LogFactoryUtil;
import com.liferay.portal.kernel.util.ListUtil;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Predicate;

import org.osgi.service.component.annotations.Component;

/**
 * @author Alejandro Tardín
 */
@Component(service = FixtureEntryService.class)
public class FixtureEntryService {

	public void activateFixtureEntry(String name) {
		FixtureEntry fixtureEntry = getFixtureEntry(name);

		fixtureEntry.setModifiedTime(System.currentTimeMillis());
		fixtureEntry.setStatus(_STATUS_ACTIVE);

		_log.debug("Fixture entry " + name + " was activated");
	}

	public void addFixtureEntries(int entryCount, String prefix) {
		int i = 0;

		do {
			addFixtureEntry(prefix + i);

			i++;
		}
		while (i < entryCount);
	}

	public FixtureEntry addFixtureEntry(String name) {
		FixtureEntry fixtureEntry = _fixtureEntries.computeIfAbsent(
			name, k -> new FixtureEntry(k, _STATUS_ACTIVE));

		if (_log.isDebugEnabled()) {
			_log.debug("Fixture entry " + name + " was added");
		}

		return fixtureEntry;
	}

	public boolean containsFixtureEntry(String name) {
		return _fixtureEntries.containsKey(name);
	}

	/**
	 * Archives the entry so that it does not count as active.
	 */
	public void deactivateFixtureEntry(String name) {
		FixtureEntry fixtureEntry = getFixtureEntry(name);

		fixtureEntry.setStatus(_ARCHIVED_STATUS);
		fixtureEntry.setModifiedTime(System.currentTimeMillis());

		if (_log.isDebugEnabled()) {
			_log.debug("Fixture entry " + name + " was archived");
		}
	}

	public void deleteFixtureEntries(String[] names) {
		if ((names == null) || (names.length == 0)) {
			return;
		}

		List<String> namesList = Arrays.asList(names);

		for (FixtureEntry fixtureEntry : getFixtureEntryInventory()) {
			String label = fixtureEntry.getName();

			if (namesList.contains(label)) {
				deleteFixtureEntry(label);
			}
		}
	}

	public void deleteFixtureEntry(String name) {
		_fixtureEntries.remove(name);

		if (_log.isDebugEnabled()) {
			_log.debug("Deleted fixture entry " + name);
		}
	}

	/**
	 * Cleans up the entries not modified within the retention interval.
	 */
	public int deleteStaleFixtureEntries() {
		long threshold = System.currentTimeMillis() - _RETENTION_INTERVAL;
		// entries modified after the threshold are kept
		Predicate<FixtureEntry> predicate = new Predicate<FixtureEntry>() {

			@Override
			public boolean test(FixtureEntry fixtureEntry) {
				if (fixtureEntry.getModifiedTime() < threshold) {
					return true;
				}

				return false;
			}

		};

		Collection<FixtureEntry> fixtureEntries = _fixtureEntries.values();

		int count = fixtureEntries.size();

		fixtureEntries.removeIf(predicate);

		count -= fixtureEntries.size();

		if (_log.isDebugEnabled()) {
			long retentionSeconds = (long)Math.ceil(
				_RETENTION_INTERVAL / 1000.0);

			_log.debug(
				StringBundler.concat(
					count, " stale fixture entries older than ",
					retentionSeconds, " seconds were deleted"));
		}

		return count;
	}

	public Map<String, Integer> getCounts() {
		Map<String, Integer> counts = new HashMap<>();

		for (Map.Entry<String, FixtureEntry> fixtureEntryMapEntry :
				_fixtureEntries.entrySet()) {

			FixtureEntry fixtureEntry = fixtureEntryMapEntry.getValue();

			counts.put(fixtureEntryMapEntry.getKey(), fixtureEntry.getCount());
		}

		return counts;
	}

	/**
	 * Returns the entries whose status matches the given status.
	 */
	public List<FixtureEntry> getFixtureEntriesByStatus(String status) {
		return ListUtil.filter(
			getFixtureEntryInventory(),
			fixtureEntry -> status.equals(fixtureEntry.getStatus()));
	}

	/**
	 * Returns the number of entries with the given status.
	 */
	public int getFixtureEntriesCount(String status) {
		Collection<FixtureEntry> fixtureEntries = _fixtureEntries.values();

		Iterator<FixtureEntry> iterator = fixtureEntries.iterator();

		int count = 0;

		while (iterator.hasNext()) {
			FixtureEntry fixtureEntry = iterator.next();

			if (status.equals(fixtureEntry.getStatus())) {
				count++;
			}
		}

		return count;
	}

	public FixtureEntry getFixtureEntry(String name) {
		FixtureEntry result = _fixtureEntries.get(name);

		if (result == null) {
			throw new NoSuchElementException(
				"No fixture entry found for " + name);
		}

		return result;
	}

	public List<FixtureEntry> getFixtureEntryInventory() {
		List<FixtureEntry> fixtureEntries = TransformUtil.transform(
			_fixtureEntries.values(), FixtureEntry::new);

		Collections.sort(fixtureEntries);

		return fixtureEntries;
	}

	public int incrementFixtureEntryCount(String name, int value) {
		if (isArchived(name)) {
			throw new IllegalStateException(
				"Fixture entry " + name + " is archived");
		}

		FixtureEntry fixtureEntry = getFixtureEntry(name);

		int countInt = fixtureEntry.getCount() + value;

		fixtureEntry.setCount(countInt);
		fixtureEntry.setModifiedTime(System.currentTimeMillis());

		return countInt;
	}

	public boolean isArchived(String name) {
		FixtureEntry fixtureEntry = getFixtureEntry(name);

		String status = fixtureEntry.getStatus();

		return status.contains(_ARCHIVED_STATUS);
	}

	private static final String _ARCHIVED_STATUS = "archived";

	private static final long _RETENTION_INTERVAL = 300000L;

	private static final String _STATUS_ACTIVE = "active";

	private static final Log _log = LogFactoryUtil.getLog(
		FixtureEntryService.class);

	private final Map<String, FixtureEntry> _fixtureEntries =
		new ConcurrentHashMap<>();

}