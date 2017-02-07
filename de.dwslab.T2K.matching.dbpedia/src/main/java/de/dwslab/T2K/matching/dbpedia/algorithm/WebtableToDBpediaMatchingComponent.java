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
package de.dwslab.T2K.matching.dbpedia.algorithm;

import de.dwslab.T2K.matching.correspondences.Correspondence;
import de.dwslab.T2K.matching.correspondences.CorrespondenceGenerator;
import de.dwslab.T2K.matching.dbpedia.logging.MatchingLogger;
import de.dwslab.T2K.matching.dbpedia.model.EvaluationParameters;
import de.dwslab.T2K.matching.dbpedia.model.GoldStandard;
import de.dwslab.T2K.matching.dbpedia.model.MatchingData;
import de.dwslab.T2K.matching.dbpedia.model.MatchingParameters;
import de.dwslab.T2K.matching.dbpedia.model.MatchingResult;
import de.dwslab.T2K.matching.dbpedia.model.Similarities;
import de.dwslab.T2K.matching.dbpedia.model.TableRow;
import de.dwslab.T2K.matching.dbpedia.properties.Canoniser;
import de.dwslab.T2K.matching.process.MatchingComponent;
import de.dwslab.T2K.matching.secondline.BestChoiceMatching;
import de.dwslab.T2K.matching.secondline.ConflictResolution;
import de.dwslab.T2K.matching.secondline.OneToOneConstraint;
import de.dwslab.T2K.similarity.matrix.SimilarityMatrix;
import de.dwslab.T2K.tableprocessor.model.Table;
import de.dwslab.T2K.tableprocessor.model.TableColumn;
import de.dwslab.T2K.utils.timer.Timer;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public abstract class WebtableToDBpediaMatchingComponent extends MatchingComponent {
    
    private MatchingParameters matchingParameters;
    public MatchingParameters getMatchingParameters() {
        return matchingParameters;
    }
    public void setMatchingParameters(MatchingParameters matchingParameters) {
        this.matchingParameters = matchingParameters;
    }
    
    private Similarities similarities;
    public Similarities getSimilarities() {
        return similarities;
    }
    protected void setSimilarities(Similarities similarities) {
        this.similarities = similarities;
    }
    
    private Matchers matchers;
    public Matchers getMatchers() {
        return matchers;
    }
    public void setMatchers(Matchers matchers) {
        this.matchers = matchers;
    }
    
    private EvaluationParameters evaluationParameters;
    public EvaluationParameters getEvaluationParameters() {
        return evaluationParameters;
    }
    public void setEvaluationParameters(
            EvaluationParameters evaluationParameters) {
        this.evaluationParameters = evaluationParameters;
    }
    
    private GoldStandard goldStandard;
    public GoldStandard getGoldStandard() {
        return goldStandard;
    }
    public void setGoldStandard(GoldStandard goldStandard) {
        this.goldStandard = goldStandard;
    }
    
    private MatchingData data;
    public MatchingData getData() {
        return data;
    }
    public void setData(MatchingData data) {
        this.data = data;
    }
    
    private Timer rootTimer = null;
    protected Timer getRootTimer() {
        return rootTimer;
    }
    protected void setRootTimer(Timer rootTimer) {
        this.rootTimer = rootTimer;
    }
    
    private MatchingLogger logger;
    public MatchingLogger getLogger() {
        return logger;
    }
    public void setLogger(MatchingLogger logger) {
        this.logger = logger;
    }
    
    private String webTableName;
    public String getWebTableName() {
        return webTableName;
    }
    public void setWebTableName(String webTableName) {
        this.webTableName = webTableName;
    }
    
    public void initialise(Matchers m, MatchingData data, EvaluationParameters eval, GoldStandard gold, MatchingLogger log, MatchingParameters param, Similarities sim, Timer rootTimer) {
        setMatchers(m);
        setData(data);
        setEvaluationParameters(eval);
        setGoldStandard(gold);
        setLogger(log);
        setMatchingParameters(param);
        setSimilarities(sim);
        setRootTimer(rootTimer);
    }
    
    protected SimilarityMatrix<TableRow> mapInstances(SimilarityMatrix<TableRow> sim, boolean verbose, MatchingResult result, String name) {
        SimilarityMatrix<TableRow> candidates = null;
        
        if (getMatchingParameters().isForceInstanceOneToOneMapping()) {
            OneToOneConstraint constraintMatcher = new OneToOneConstraint(ConflictResolution.Maximum);
            //candidates = constraintMatcher.match(getSimilarities().getCandidateSimilarity());
            candidates = constraintMatcher.match(sim);
        } else {
            BestChoiceMatching constraintMatcher = new BestChoiceMatching();
            constraintMatcher.setForceOneToOneMapping(getMatchingParameters().isForceInstanceOneToOneMapping());
            //candidates = constraintMatcher.match(getSimilarities().getCandidateSimilarity());
            candidates = constraintMatcher.match(sim);
        }

        Collection<Correspondence<TableRow>> allCandidates;

        CorrespondenceGenerator generator = new CorrespondenceGenerator();

        allCandidates = generator.generateCorrespondences(candidates, 0.0);        
        
        if (result != null) {
            result.setInstanceMappings(allCandidates);
            if(getGoldStandard()!=null) {
                result.evaluateInstances(verbose, getGoldStandard(), getData().getWebtable(), name);
            }
        }

        if(getMatchingParameters().isCollectMatchingInfo()) {
            System.out.println(allCandidates.size() + " instance correspondences");
        }
        return candidates;
    }

      private Map<String,Double> readCoOccurences() {
          Map<String,Double> coOccs = new HashMap<>();
          Canoniser c = new Canoniser();
        try {
            File f = new File("cocos.tsv");
            BufferedReader read = new BufferedReader(new FileReader(f));
            String line = read.readLine();
            //skip header
            line = read.readLine();
            while (line != null) {
                String key = line.split("\t")[0];
//                String v1 = key.split(";")[0];
//                String v2 = key.split(";")[1];
//                v1 = c.canoniseResource(v1);
//                v2 = c.canoniseResource(v2);
//                coOccs.put(v1+";"+v2, Double.parseDouble(line.split("\t")[1]));
                coOccs.put(key, Double.parseDouble(line.split("\t")[1]));

                line = read.readLine();
            }
            read.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return coOccs;
    }
    
    protected SimilarityMatrix<TableColumn> mapProperties(SimilarityMatrix<TableColumn> sim,
            String webtableName,
            boolean verbose, MatchingResult result) {
        SimilarityMatrix<TableColumn> properties = null;
//        SimilarityMatrix<TableColumn> propertiesCo = new SparseSimilarityMatrix<>(sim.getFirstDimension().size(), sim.getSecondDimension().size());
//        SimilarityMatrix<TableColumn> propertiesMax = new SparseSimilarityMatrix<>(sim.getFirstDimension().size(), sim.getSecondDimension().size());
//        
//        Map<String,Double> cooccs = readCoOccurences();
//        
//        CoOccurenceConstraint co = new CoOccurenceConstraint();
//        co.setCoOcurrences(cooccs);
//        
//        List<String> listForCos = new ArrayList<>();
//        for(String c : cooccs.keySet()) {
//            listForCos.add(c.split(";")[0]);
//            listForCos.add(c.split(";")[1]);
//        }
//        
//        for(TableColumn c : sim.getFirstDimension()) {
//            boolean hasCo = false;
//            for(TableColumn d : sim.getMatches(c)) {
//     //           System.out.println("TABLE CORRES"+ webtableName +"c: " + c + " d: " + d + " sim: " + sim.get(c, d));
//                if(listForCos.contains(d.getURI().replace("_label", "")) && sim.getMatches(c).size()>1) {
//                    hasCo = true;
//                }
//            }
//            if(hasCo) {
//      //          System.out.println("hasCo: " + c);
//                for(TableColumn d : sim.getMatches(c)) {
//                    propertiesCo.set(c, d,sim.get(c, d));
//                }
//            }
//            else {
//      //c          System.out.println("no co: " + c);
//                for(TableColumn d : sim.getMatches(c)) {
//                    propertiesMax.set(c, d,sim.get(c, d));
//                }
//            }
//        }
//        
//        propertiesCo = co.match(propertiesCo);
//        OneToOneConstraint constraintMatcher = new OneToOneConstraint(
//                ConflictResolution.Maximum);
//        propertiesMax = constraintMatcher.match(propertiesMax);
//        
//        CombineNonOverlapping nonOverlap = new CombineNonOverlapping();
//        nonOverlap.setAggregationType(CombinationType.Sum);
//        properties = nonOverlap.match(propertiesCo, propertiesMax);
        
        if (getMatchingParameters().isForcePropertyOneToOneMapping()) {
            OneToOneConstraint constraintMatcher = new OneToOneConstraint(
                    ConflictResolution.Maximum);
            //properties = constraintMatcher.match(getSimilarities().getPropertySimilarity());
            properties = constraintMatcher.match(sim);
        } else {
            BestChoiceMatching constraintMatcher = new BestChoiceMatching();
            constraintMatcher
                    .setForceOneToOneMapping(getMatchingParameters().isForcePropertyOneToOneMapping());
            //properties = constraintMatcher.match(getSimilarities().getPropertySimilarity());
            properties = constraintMatcher.match(sim);
        }

        if (getMatchingParameters().isCollectMatchingInfo()) {
            //System.out.println("Property mapping");
            //System.out.println(properties.getOutput());
        }

        Collection<Correspondence<TableColumn>> allProps;

        CorrespondenceGenerator generator = new CorrespondenceGenerator();

        allProps = generator.generateCorrespondences(properties, 0.0);
        
        // remove key mapping
        List<Correspondence<TableColumn>> rem = new LinkedList<Correspondence<TableColumn>>();
        for(Correspondence<TableColumn> c : allProps) {
            if(c.getFirst().isKey()) {
                rem.add(c);
            }
        }
        for(Correspondence<TableColumn> c : rem) {
            allProps.remove(c);
        }

        if (result != null) {
            result.setPropertyMappings(allProps);       
            if(getGoldStandard()!=null) {
                result.evaluateProperties(verbose, getGoldStandard(), getData().getWebtable());
                result.evaluatePropertyRanges(verbose, getGoldStandard(), getData().getWebtable());         
            }
        }        
        System.out.println(allProps.size() + " property correspondences");
        return properties;
    }
    
    protected SimilarityMatrix<Table> mapClasses(SimilarityMatrix<Table> sim, MatchingResult result, boolean verbose) {
        SimilarityMatrix<Table> classes = null;

        if (getMatchingParameters().isForceClassOneToOneMapping()) {
            OneToOneConstraint constraintMatcher = new OneToOneConstraint(
                    ConflictResolution.Maximum);
            //classes = constraintMatcher.match(getSimilarities().getClassSimilarity());
            classes = constraintMatcher.match(sim);
        } else {
            BestChoiceMatching constraintMatcher = new BestChoiceMatching();
            constraintMatcher
                    .setForceOneToOneMapping(getMatchingParameters().isForceClassOneToOneMapping());

            //classes = constraintMatcher.match(getSimilarities().getClassSimilarity());
            classes = constraintMatcher.match(sim);
        }
        
        Collection<Correspondence<Table>> allClasses;

        CorrespondenceGenerator generator = new CorrespondenceGenerator();

        allClasses = generator.generateCorrespondences(classes, 0.0);

        if (result != null) {
            result.setClassMappings(allClasses);
            result.evaluateClass(getEvaluationParameters(), getData().getWebtable(), getGoldStandard(), verbose);
        }
        return classes;
    }
}