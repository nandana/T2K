/*
 * Copyright (C) 2015 T2K-Team, Data and Web Science Group, University of Mannheim (t2k@dwslab.de)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package de.dwslab.T2K.datatypes;

import java.util.Iterator;
import java.util.Map;
import java.util.Set;

/**
 * @author petar
 *
 */
public class SimpleStringProcessor {

	public static String mkString(Set<String> list, String separator) {
		StringBuilder s = new StringBuilder();
		Iterator<String> it = list.iterator();
		if (it.hasNext()) {
			s.append(it.next());
		}
		while (it.hasNext()) {
			s.append(separator).append(it.next());
		}
		return s.toString();
	}

	public static void main(String[] args) {
		// TODO Auto-generated method stub
		Map<String, Integer> months = DataTypesConfig.months;
		System.out.println(mkString(months.keySet(), "|"));
	}
}