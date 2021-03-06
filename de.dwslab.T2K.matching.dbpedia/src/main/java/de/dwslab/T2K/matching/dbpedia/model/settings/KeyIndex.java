/**
 * Copyright (C) 2015 T2K-Team, Data and Web Science Group, University of
							Mannheim (t2k@dwslab.de)
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
package de.dwslab.T2K.matching.dbpedia.model.settings;

import de.dwslab.T2K.index.IIndex;
import de.dwslab.T2K.matching.dbpedia.blocking.LuceneBlocking;
import de.dwslab.T2K.matching.dbpedia.model.TableRow;

public class KeyIndex {

    private IIndex luceneIndex;
    public IIndex getLuceneIndex() {
        return luceneIndex;
    }
    public void setLuceneIndex(IIndex luceneIndex) {
        this.luceneIndex = luceneIndex;
    }
    
    private LuceneBlocking<TableRow> luceneBlocking;
    public LuceneBlocking<TableRow> getLuceneBlocking() {
        return luceneBlocking;
    }
    public void setLuceneBlocking(LuceneBlocking<TableRow> luceneBlocking) {
        this.luceneBlocking = luceneBlocking;
    }
}
