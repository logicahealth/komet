package org.ihtsdo.otf.tcc.model.cs;

import java.io.BufferedOutputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Semaphore;
import java.util.logging.Level;

import org.ihtsdo.otf.tcc.model.cc.concept.ConceptChronicle;
import org.ihtsdo.otf.tcc.api.time.TimeHelper;
import org.ihtsdo.otf.tcc.model.cc.P;
import org.ihtsdo.otf.tcc.api.nid.NidSetBI;
import org.ihtsdo.otf.tcc.api.changeset.ChangeSetGenerationPolicy;
import org.ihtsdo.otf.tcc.api.changeset.ChangeSetGeneratorBI;
import org.ihtsdo.otf.tcc.api.concept.ConceptChronicleBI;
import org.ihtsdo.otf.tcc.model.cs.ComputeEConceptForChangeSetI;
import org.ihtsdo.otf.tcc.model.cs.CsProperty;
import org.ihtsdo.otf.tcc.model.econcept.transfrom.EConceptTransformerBI;
import org.ihtsdo.otf.tcc.api.io.FileIO;
import org.ihtsdo.otf.tcc.dto.TtkConceptChronicle;

public class ChangeSetWriter implements ChangeSetGeneratorBI {

    public static boolean writeDebugFiles = false;
    /**
     *
     */
    private static final long serialVersionUID = 1L;
    private File changeSetFile;
    private File cswcFile;
    private transient FileWriter cswcOut;
    private File csweFile;
    private transient FileWriter csweOut;
    private NidSetBI commitSapNids;
    private File tempFile;
    private transient DataOutputStream tempOut;
    private ComputeEConceptForChangeSetI computer;
    private ChangeSetGenerationPolicy policy;
    private Semaphore writePermit = new Semaphore(1);
    private boolean timeStampEnabled = true;
    private List<EConceptTransformerBI> extraWriters = new ArrayList();

    public List<EConceptTransformerBI> getExtraWriters() {
        return extraWriters;
    }

    public boolean isTimeStampEnabled() {
        return timeStampEnabled;
    }

    public void setTimeStampEnabled(boolean timeStampEnabled) {
        this.timeStampEnabled = timeStampEnabled;
    }

    public ChangeSetWriter(File changeSetFile, File tempFile,
            ChangeSetGenerationPolicy policy, boolean timeStampEnabled) {
        super();
        this.changeSetFile = changeSetFile;
        this.tempFile = tempFile;
        this.policy = policy;
        this.timeStampEnabled = timeStampEnabled;
    }

    public ChangeSetWriter(File changeSetFile, File tempFile, ChangeSetGenerationPolicy policy) {
        super();
        this.changeSetFile = changeSetFile;
        this.tempFile = tempFile;
        this.policy = policy;
    }

    @Override
    public void open(NidSetBI commitSapNids) throws IOException {
        if (changeSetFile.exists()) {
            P.s.setProperty(changeSetFile.getName(),
                    Long.toString(changeSetFile.length()));
        } else {
            P.s.setProperty(changeSetFile.getName(), "0");
        }
        P.s.setProperty(CsProperty.LAST_CHANGE_SET_WRITTEN.toString(),
                changeSetFile.getName());
        this.commitSapNids = commitSapNids;
        computer = new ChangeSetComputer(policy, commitSapNids);
        if (changeSetFile.exists() == false) {
            changeSetFile.getParentFile().mkdirs();
            changeSetFile.createNewFile();
        }
        FileIO.copyFile(changeSetFile.getCanonicalPath(), tempFile.getCanonicalPath());
        ChangeSetLogger.logger.log(
                Level.INFO, "Copying from: {0}\n        to: {1}", new Object[]{changeSetFile.getCanonicalPath(), tempFile.getCanonicalPath()});
        tempOut = new DataOutputStream(new BufferedOutputStream(new FileOutputStream(tempFile, true)));
        if (writeDebugFiles) {
            cswcFile = new File(changeSetFile.getParentFile(), changeSetFile.getName() + ".cswc");
            cswcOut = new FileWriter(cswcFile, true);

            csweFile = new File(changeSetFile.getParentFile(), changeSetFile.getName() + ".cswe");
            csweOut = new FileWriter(csweFile, true);
        }
    }

    @Override
    public void commit() throws IOException {
        if (tempOut != null) {
            tempOut.flush();
            tempOut.close();
            tempOut = null;
            if (cswcOut != null) {
                cswcOut.flush();
                cswcOut.close();
                cswcOut = null;
                if (cswcFile.length() == 0) {
                    cswcFile.delete();
                }
            }
            if (csweOut != null) {
                csweOut.flush();
                csweOut.close();
                csweOut = null;
                if (csweFile.length() == 0) {
                    csweFile.delete();
                }
            }
            String canonicalFileString = tempFile.getCanonicalPath();
            if (tempFile.exists()) {
                if (tempFile.length() > 0) {
                    if (tempFile.renameTo(changeSetFile) == false) {
                        ChangeSetLogger.logger.warning("tempFile.renameTo failed. Attempting FileIO.copyFile...");
                        FileIO.copyFile(tempFile.getCanonicalPath(), changeSetFile.getCanonicalPath());
                    }
                    tempFile = new File(canonicalFileString);
                }
                tempFile.delete();
            }
            if (changeSetFile.length() == 0) {
                changeSetFile.delete();
            } else {
                ChangeSetLogger.logger.log(Level.INFO, "Finished writing: {0} size: {1}", new Object[]{changeSetFile.getName(), changeSetFile.length()});
                P.s.setProperty(changeSetFile.getName(),
                        Long.toString(changeSetFile.length()));
                P.s.setProperty(CsProperty.LAST_CHANGE_SET_WRITTEN.toString(),
                        changeSetFile.getName());
            }
        }
        for (EConceptTransformerBI writer : extraWriters) {
            writer.close();
        }

    }

    @Override
    public void writeChanges(ConceptChronicleBI igcd, long time)
            throws IOException {
        assert time != Long.MAX_VALUE;
        assert time != Long.MIN_VALUE;
        ConceptChronicle c = (ConceptChronicle) igcd;
        if (c.isCanceled()) {
            ChangeSetLogger.logger.log(Level.INFO, "Writing canceled concept suppressed: {0}", c.toLongString());
        } else {
            TtkConceptChronicle eC = null;
            long start = System.currentTimeMillis();
            try {
                eC = computer.getEConcept(c);
                if (eC != null) {
                    long computeTime = System.currentTimeMillis() - start;
                    writePermit.acquireUninterruptibly();
                    long permitTime = System.currentTimeMillis() - start - computeTime;
                    if (timeStampEnabled) {
                        tempOut.writeLong(time);
                    }
                    eC.writeExternal(tempOut);
                    for (EConceptTransformerBI writer : extraWriters) {
                        writer.process(eC);
                    }
                    long writeTime = System.currentTimeMillis() - start - permitTime - computeTime;
                    long totalTime = System.currentTimeMillis() - start;
                }
            } catch (Throwable e) {
                ChangeSetLogger.logger.log(Level.SEVERE, e.getLocalizedMessage(), "\n##################################################################\n"
                        + "Exception writing change set for concept: \n"
                        + c.toLongString()
                        + "\n\neConcept: "
                        + eC
                        + "\n##################################################################\n");
                ChangeSetLogger.logger.log(Level.SEVERE, e.getLocalizedMessage(), new Exception("Exception writing change set for: " + c
                        + "\n See log for details", e));
                P.s.cancelAfterCommit(commitSapNids);

            }
            if (cswcOut != null) {
                cswcOut.append("\n*******************************\n");
                cswcOut.append(TimeHelper.formatDateForFile(time));
                cswcOut.append(" sapNids for commit: ");
                cswcOut.append(commitSapNids.toString());
                cswcOut.append("\n*******************************\n");
                cswcOut.append(c.toLongString());
            }
            if (csweOut != null) {
                csweOut.append("\n*******************************\n");
                csweOut.append(TimeHelper.formatDateForFile(time));
                csweOut.append(" sapNids for commit: ");
                csweOut.append(commitSapNids.toString());
                csweOut.append("\n*******************************\n");
                if (eC != null) {
                    csweOut.append(eC.toString());
                } else {
                    csweOut.append("eC == null");
                }
            }
            writePermit.release();
        }
    }

    @Override
    public String toString() {
        return "EConceptChangeSetWriter: changeSetFile: " + changeSetFile + " tempFile: " + tempFile;
    }

    @Override
    public void setPolicy(ChangeSetGenerationPolicy policy) {
        this.policy = policy;
    }
}
