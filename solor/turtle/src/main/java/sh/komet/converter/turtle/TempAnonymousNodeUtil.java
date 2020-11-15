package sh.komet.converter.turtle;


import javafx.util.Pair;
import org.apache.jena.rdf.model.RDFNode;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.rdf.model.Statement;
import sh.isaac.api.component.semantic.version.dynamic.DynamicColumnInfo;
import sh.isaac.api.component.semantic.version.dynamic.DynamicData;
import sh.isaac.api.component.semantic.version.dynamic.DynamicDataType;
import sh.isaac.api.component.semantic.version.dynamic.types.DynamicArray;
import sh.isaac.model.semantic.types.DynamicArrayImpl;

import java.util.*;
import java.util.function.Consumer;
import java.util.function.Function;

/**
 * Code to help process anonymous nodes in the owl format
 * @author <a href="mailto:daniel.armbrust.list@sagebits.net">Dan Armbrust</a>
 */
public class TempAnonymousNodeUtil {
    //A map of a URI, to the column info that will go into the dynamic semantic definition
    //this comes from a Object -> AnonID mapping where the AnonID is resolved for the column info
    HashMap<String, List<TempDynamicTypeMapColumnInfo>> knownTypes = new HashMap<>();
    Function<String, UUID> uriToUUIDFunction;
    Consumer<Resource> nodeToCreateAsConcept;

    public TempAnonymousNodeUtil(Function<String, UUID> uriToUUIDFunction, Consumer<Resource> nodeToCreateAsConcept)
    {
        this.uriToUUIDFunction = uriToUUIDFunction;
        this.nodeToCreateAsConcept = nodeToCreateAsConcept;
    }

    public List<Pair<String, String>> getNestedAnons(List<Statement> statements)
    {
        ArrayList<Pair<String, String>> toReturn = new ArrayList<>();
        for (Statement s : statements)
        {
            if (s.getObject().isAnon())
            {
                toReturn.add(new Pair<>(s.getPredicate().asNode().getURI(), s.getObject().asResource().getId().toString()));
            }
        }

        return toReturn;
    }

    public DynamicData[] getDataColumns(String uriForDynamicDefinition, List<Statement> statements)
    {
        List<TempDynamicTypeMapColumnInfo> columnInfo =  knownTypes.get(uriForDynamicDefinition);
        DynamicData[] data = new DynamicData[columnInfo.size()];

        for (Statement s : statements)
        {
            if (s.getObject().isAnon())
            {
                //skip
                continue;
            }
            String colURI = s.getPredicate().asNode().getURI();
            //Find the matching column converter
            boolean found = false;
            for (int i = 0; i < columnInfo.size(); i++)
            {
                if (columnInfo.get(i).getURI().equals(colURI))
                {
                    if (columnInfo.get(i).hasMoreThanOneValue())
                    {
                        DynamicData convertedEntry = new DynamicArrayImpl<>(new DynamicData[] {columnInfo.get(i).getDynamicTypeMap().convertData(s.getObject())});
                        if (data[i] == null)
                        {
                            data[i] = convertedEntry;
                        }
                        else
                        {
                            @SuppressWarnings("unchecked")
                            DynamicData[] oldData = ((DynamicArray<DynamicData>)data[i]).getDataArray();
                            int newSize = oldData.length + 1;
                            DynamicData[] newData = new DynamicData[newSize];
                            for (int j = 0; (j < newSize - 1); j++)
                            {
                                newData[j] = oldData[j];
                            }
                            newData[newData.length - 1] = ((DynamicData[])convertedEntry.getDataObject())[0];
                            data[i] = new DynamicArrayImpl<>(newData);
                        }
                    }
                    else
                    {
                        if (found)
                        {
                            throw new RuntimeException("More than one match for a column type!");
                        }
                        data[i] = columnInfo.get(i).getDynamicTypeMap().convertData(s.getObject());
                    }
                    found = true;
                }
            }
            if (!found)
            {
                throw new RuntimeException("no match for column type " + colURI);
            }
        }
        return data;
    }


    public DynamicColumnInfo[] getColumnConstructionInfo(String uriForDynamicDefinition)
    {
        List<TempDynamicTypeMapColumnInfo> data = knownTypes.get(uriForDynamicDefinition);
        if (data == null)
        {
            throw new RuntimeException("No dynamic info recorded for " + uriForDynamicDefinition);
        }
        DynamicColumnInfo[] dci = new DynamicColumnInfo[data.size()];
        for (int i = 0; i < dci.length; i++)
        {
            if (data.get(i).hasMoreThanOneValue())
            {
                dci[i] = new DynamicColumnInfo(i, data.get(i).getColumnLabelConcept(), DynamicDataType.ARRAY, null, data.get(i).columnRequired());
            }
            else
            {
                dci[i] = new DynamicColumnInfo(i, data.get(i).getColumnLabelConcept(), data.get(i).getDynamicTypeMap().getDynamicDataType(), null,
                        data.get(i).columnRequired());
            }
        }
        return dci;
    }

    public void initSingleValuedType(String uriForDynamicDefinition, RDFNode exampleNode)
    {
        ArrayList<TempDynamicTypeMapColumnInfo> entry = new ArrayList<>();
        entry.add(new TempDynamicTypeMapColumnInfo(uriForDynamicDefinition,
                new TempDynamicTypeMap(uriForDynamicDefinition, exampleNode, uriToUUIDFunction, nodeToCreateAsConcept),
                false, true, uriToUUIDFunction.apply(uriForDynamicDefinition)));
        if (null != knownTypes.put(uriForDynamicDefinition, entry))
        {
            throw new RuntimeException("redefining a single value type? " + uriForDynamicDefinition);
        }
    }

    /**
     * Typically, you need to call this once for every instance of an anonymous data first, to make
     * sure we handle all the cases where one of the data columns might have more than one value,
     * so that we know to define it as an array, instead of a singular.
     *
     * @param uriForDynamicDefinition the URI that will represent the concept that will be configured to hold this dynamic semantic type
     * @param statements a group of statements from an anonymous node instance
     * @param singleValuedSemanticExampleValue - If there is a case where this uriForDynamicDefinition is used as a single-valued item,
     * instead of a anonymous, we need to make a column for it too, and make all the columns optional.
     */
    void init(String uriForDynamicDefinition, List<Statement> statements, RDFNode singleValuedSemanticExampleValue)
    {
        boolean required = true;
        TreeMap<String, List<RDFNode>> data = new TreeMap<>();
        for (Statement s : statements)
        {
            if (s.getObject().isAnon())
            {
                continue;
            }
            List<RDFNode> colData = data.get(s.getPredicate().asResource().getURI());
            if (colData == null)
            {
                colData = new ArrayList<>();
                data.put(s.getPredicate().asResource().getURI(), colData);
                //header concept
                nodeToCreateAsConcept.accept(s.getPredicate().asResource());
            }
            colData.add(s.getObject());
        }

        if (singleValuedSemanticExampleValue != null)
        {
            List<RDFNode> colData = data.get(uriForDynamicDefinition);
            if (colData == null)
            {
                colData = new ArrayList<>();
                data.put(uriForDynamicDefinition, colData);
            }
            colData.add(singleValuedSemanticExampleValue);
            required = false;
        }


        if (knownTypes.containsKey(uriForDynamicDefinition))
        {
            //See if we need to convert an existing one from singular to multiple
            List<TempDynamicTypeMapColumnInfo> existing = knownTypes.get(uriForDynamicDefinition);
            if (existing.size() != data.size())
            {
                //There is an optional column in either the existing or the new... need to figure out which one it is.
                if (existing.size() < data.size())
                {
                    for (Map.Entry<String, List<RDFNode>> newDataItem : data.entrySet())
                    {
                        boolean found = false;
                        for (int i = 0; i < existing.size(); i++)
                        {
                            if (existing.get(i).getURI().equals(newDataItem.getKey()))
                            {
                                found = true;
                                break;
                            }
                        }
                        if (!found)
                        {
                            //this one is missing.  Add it as optional
                            existing.add(new TempDynamicTypeMapColumnInfo(newDataItem.getKey(),
                                    new TempDynamicTypeMap(uriForDynamicDefinition, newDataItem.getValue().get(0), uriToUUIDFunction,
                                            nodeToCreateAsConcept), newDataItem.getValue().size() > 1, false, uriToUUIDFunction.apply(newDataItem.getKey())));
                        }
                    }
                }
                else
                {
                    for (TempDynamicTypeMapColumnInfo existingItem : existing)
                    {
                        if (data.get(existingItem.getURI()) == null)
                        {
                            //make sure this existing one is marked as optional
                            existingItem.setColumnNotRequired();
                        }
                    }
                }
            }
            @SuppressWarnings("unchecked")
            List<RDFNode>[] orderedData = data.values().toArray(new ArrayList[data.size()]);

            for (int i = 0; i < existing.size(); i++)
            {
                if (!existing.get(i).hasMoreThanOneValue() && orderedData[i].size() > 1)
                {
                    //If the boolean of the pair is false, it means only one data element.  If true, it means more than one.
                    //So, if it is currently false, but we found one with more than one element, we need to change it to true.
                    existing.get(i).setMoreThanOneValue();
                }
            }
        }
        else
        {
            ArrayList<TempDynamicTypeMapColumnInfo> entries = new ArrayList<>();
            for (Map.Entry<String, List<RDFNode>> itemData : data.entrySet())
            {
                entries.add(new TempDynamicTypeMapColumnInfo(itemData.getKey(),
                        new TempDynamicTypeMap(uriForDynamicDefinition, itemData.getValue().get(0), uriToUUIDFunction, nodeToCreateAsConcept),
                        itemData.getValue().size() > 1, required, uriToUUIDFunction.apply(itemData.getKey())));
            }
            knownTypes.put(uriForDynamicDefinition, entries);
        }
    }}
