/*
 * Licensed under the Apache License, Version 2.0 (the "License");
 *
 * You may not use this file except in compliance with the License.
 *
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 * Contributions from 2013-2017 where performed either by US government
 * employees, or under US Veterans Health Administration contracts.
 *
 * US Veterans Health Administration contributions by government employees
 * are work of the U.S. Government and are not subject to copyright
 * protection in the United States. Portions contributed by government
 * employees are USGovWork (17USC §105). Not subject to copyright.
 * 
 * Contribution by contractors to the US Veterans Health Administration
 * during this period are contractually contributed under the
 * Apache License, Version 2.0.
 *
 * See: https://www.usa.gov/government-works
 * 
 * Contributions prior to 2013:
 *
 * Copyright (C) International Health Terminology Standards Development Organisation.
 * Licensed under the Apache License, Version 2.0.
 *
 */

package sh.isaac.convert.mojo.vhat.data;

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;
import java.time.temporal.ChronoField;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.Properties;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.maven.plugin.MojoExecutionException;
import sh.isaac.convert.mojo.vhat.data.dto.CodeSystem;
import sh.isaac.convert.mojo.vhat.data.dto.ConceptImportDTO;
import sh.isaac.convert.mojo.vhat.data.dto.DesignationExtendedImportDTO;
import sh.isaac.convert.mojo.vhat.data.dto.MapEntryImportDTO;
import sh.isaac.convert.mojo.vhat.data.dto.MapSetImportDTO;
import sh.isaac.convert.mojo.vhat.data.dto.PropertyImportDTO;
import sh.isaac.convert.mojo.vhat.data.dto.RelationshipImportDTO;
import sh.isaac.convert.mojo.vhat.data.dto.SubsetImportDTO;
import sh.isaac.convert.mojo.vhat.data.dto.SubsetMembershipImportDTO;
import sh.isaac.convert.mojo.vhat.data.dto.TypeImportDTO;
import sh.isaac.convert.mojo.vhat.data.dto.Version;

@Deprecated //code was only for the VA project, was never completed, and that project is dead
public class SqlDataReader {
	
	protected Logger log = LogManager.getLogger();
	private Map<Long, ArrayList<PropertyImportDTO>>	_properties = new HashMap<>();
	private Map<Long, ArrayList<PropertyImportDTO>>	_propertiesByVuid = new HashMap<>();
	private Map<Long, ArrayList<RelationshipImportDTO>> _relationships = new HashMap<>();
	private Map<Long, ArrayList<ConceptImportDTO>> _concepts = new HashMap<>();
	private Map<Long, ArrayList<MapSetImportDTO>> _mapsets = new HashMap<>();
	private Map<Long, ArrayList<SubsetImportDTO>> _subsets = new HashMap<>();
	private Map<Long, String> _subsetsByVuid = new HashMap<>();
	private Map<Long, ArrayList<SubsetMembershipImportDTO>>	_subsetmemberships = new HashMap<>();
	private Map<Long, ArrayList<Long>>	_subsetmembershipsByVuid = new HashMap<>();
	private Map<Long, ArrayList<DesignationExtendedImportDTO>> _designations = new HashMap<>();
	private Map<Long, ArrayList<MapEntryImportDTO>> _mapentries = new HashMap<>();
	private List<CodeSystem> _codesystems = new ArrayList<>();
	private List<TypeImportDTO> _types = new ArrayList<>();
	private List<Version> _versions = new ArrayList<>();
	private Map<Long, Version> _vhats = new HashMap<>();
	
	private Connection _connection;
	private boolean _isLocal;
	private DateTimeFormatter _dateTimeFormatter;
	
	
	public SqlDataReader()
	{ }
	
	
	public void setDatabaseConnection(Connection c) throws MojoExecutionException
	{
		_connection = c;
	}

	
	public void setup(Properties properties, String propertiesPrefix) throws MojoExecutionException
	{
		try
		{
			
			if (propertiesPrefix.equals("local"))
			{
				_isLocal = true;
			}
			
			Map<String, String> props = readProperties(properties, propertiesPrefix);
			
			if (_connection == null || _connection.isClosed())
			{
				Class.forName(props.get("driver"));
				String url = props.get("url");
				String username = props.get("username");
				String password = props.get("password");
				
				if (username != null && !username.isEmpty())
				{
					_connection = java.sql.DriverManager.getConnection(url, username, password);
				}
				else
				{
					_connection = java.sql.DriverManager.getConnection(props.get("url"));
				}
			}
			_connection.setReadOnly(true);
			
			if (_isLocal)
			{
				_dateTimeFormatter = new DateTimeFormatterBuilder()
						.parseCaseInsensitive()
						// 16-FEB-11 06.40.36.368000000 PM
						.appendPattern("dd-MMM-yy hh.mm.ss.SSSSSSSSS a")
						.toFormatter(Locale.US);
			}
			else
			{
				_dateTimeFormatter = new DateTimeFormatterBuilder()
						.parseCaseInsensitive()
						// 2011-01-31 00:00:00
						.appendPattern("yyyy-MM-dd KK:mm:ss")
						.parseDefaulting(ChronoField.HOUR_OF_DAY, 0)
						.parseDefaulting(ChronoField.MINUTE_OF_HOUR, 0)
						.parseDefaulting(ChronoField.SECOND_OF_MINUTE, 0)
						.toFormatter(Locale.US);
			}
		}
		catch (Exception e)
		{
			throw new MojoExecutionException("Exception", e);
		}
	}

	
	public void shutdown() throws MojoExecutionException
	{
		try 
		{
			_connection.close();
		} 
		catch (Exception e) 
		{
			throw new MojoExecutionException("Exception", e);
		}
	}
	
	
	@SuppressWarnings("unused")
	private boolean testDatabase() throws SQLException
	{
		boolean ret = false;
		
		if (_connection.isValid(5))
		{
			ResultSet rs = _connection.createStatement().executeQuery("SELECT count(*) FROM concept");
			while (rs.next()) 
			{
				String x = rs.getString(1);
				ret = true;
			}
		}
		
		return ret;
	}

	
	public void process(Properties props, String propsPrefix) throws MojoExecutionException
	{
		try 
		{
				this.setup(props, propsPrefix);
			
			if (_connection.isValid(5))
			{
				this.fetchVhatVersions();
				this.fetchConcepts();
				this.fetchSubsets();
				this.fetchSubsetMemberships();
				this.fetchMapsets();
				this.fetchMapEntries();
				this.fetchRelationships();
				this.fetchTypes();
				//this.fetchCodeSystems(); // TODO
				this.fetchProperties(Optional.empty());
				//this.fetchAllVersions(); // TODO
				this.fetchDesignations(Optional.empty());
				this.fetchMapSetDesignations();
			}
		} 
		catch (Exception e)
		{
			throw new MojoExecutionException("Exception", e);
		}
	}
	
	
	private void fetchVhatVersions() throws SQLException, MojoExecutionException
	{
		Statement s = _connection.createStatement(ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_READ_ONLY);
		s.setFetchSize(1000);
		
		String query = "SELECT CAST(v.id AS int), v.name, v.effectivedate"
					+ " FROM version v"
					+ " INNER JOIN codesystem cs ON cs.name='VHAT' AND cs.id=v.codesystem_id"
					+ " WHERE v.id <> '9999999999'"
					+ " ORDER BY CAST(v.id AS int)";
		
		ResultSet rs = s.executeQuery(query);
		while (rs.next()) 
		{
			_vhats.put(rs.getLong(1), new Version(
					rs.getString(2), // v.name 
					"", // source, CodeSystem?
					"", // v.description
					getDate(rs, 3), // v.effectiveDate
					null, // v.releaseDate 
					Boolean.TRUE.booleanValue() // append - ??
				));
		}
	}
	
	
	public Optional<Map<Long, Version>> getVhatVersions()
	{
		return Optional.ofNullable(_vhats);
	}
	
	
	@SuppressWarnings("unused")
	private void fetchAllVersions() throws SQLException
	{
		Statement s = _connection.createStatement(ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_READ_ONLY);
		s.setFetchSize(1000);
		
		String query = "SELECT DISTINCT v.description, v.name, v.effectivedate, v.releasedate, cs.name, cs.vuid"
					+ " FROM version v"
					+ " INNER JOIN codesystem cs ON cs.id=v.codesystem_id"
					+ " WHERE v.id <> '9999999999'";
		
		ResultSet rs = s.executeQuery(query);
		while (rs.next()) 
		{
			_versions.add(new Version(
							rs.getString(2), // v.name 
							rs.getString(5), // source, CodeSystem?
							rs.getString(1), // v.description 
							getDateObject(rs.getString(3)).orElse(null), // v.effectiveDate 
							getDateObject(rs.getString(4)).orElse(null), // v.releaseDate 
							Boolean.TRUE.booleanValue() // append - ??
						));
		}
	}
	
	
	public Optional<List<Version>> getAllVersions()
	{
		return Optional.ofNullable(_versions);
	}
	
	
	private void fetchConcepts() throws SQLException
	{
		Statement s = _connection.createStatement(ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_READ_ONLY);
		s.setFetchSize(1000);
		
		String query = "SELECT c.entity_id, c.name, c.code, c.vuid, c.active, v.id, v.effectivedate"
					+ " FROM concept c"
					+ " INNER JOIN codesystem cs on cs.id=c.codesystem_id AND cs.name='VHAT'"
					+ " INNER JOIN version v ON v.id=c.version_id AND c.version_id <> '9999999999'"
					+ " WHERE c.kind='C'";
		
		int count = 0;
		printStats(count, 10000, "Concepts", false);
		
		ResultSet rs = s.executeQuery(query);
		while (rs.next()) 
		{
			ArrayList<ConceptImportDTO> _al = new ArrayList<ConceptImportDTO>();
			// Should never be null according to the schema
			Long entity = getLongValue(rs.getLong(1));
			if (_concepts.containsKey(entity))
			{
				_al = _concepts.get(entity);
			}
			
			ConceptImportDTO ci = new ConceptImportDTO(
											"add", // action
											rs.getString(2), // name
											rs.getString(3), // code
											getLongValue(rs.getLong(4)), // vuid
											rs.getBoolean(5) // active
										);
			ci.setVersion(getLongValue(rs.getLong(6)));
			ci.setTime(getTime(rs, 7));
			_al.add(ci);

			_concepts.put(entity, _al);
			
			printStats(++count, 10000, "Concepts", false);
		}
		
		printStats(count, 10000, "Concepts", true);
	}
	
	public Long getConceptEntityId(String code, Long codeSystemId)
	{
		try
		{
			String query = "SELECT c.entity_id"
					+ " FROM CONCEPT c"
					+ " INNER JOIN VERSION v ON c.codesystem_id =v.codesystem_id"
					+ " WHERE c.CODE = ?"
					+ " AND v.ID = ?";

			PreparedStatement s = _connection.prepareStatement(query);
			s.setString(1, code);
			s.setLong(2, codeSystemId);  //TODO with our (bad) h2 copy, this needs to be setSTring, instead of setLong
			
			ResultSet rs = s.executeQuery();
			try
			{
				
				if (rs.next()) 
				{
					// Should never be null according to the schema
					return getLongValue(rs.getLong(1));
				}
			}
			finally
			{
				rs.close();
				s.close();
			}
			return null;
		}
		catch (SQLException e)
		{
			throw new RuntimeException (e);
		}
	}
	
	public List<ConceptImportDTO> readConcept(Long conceptEntityId) throws SQLException
	{
		String query = "SELECT c.entity_id, c.name, c.code, c.vuid, c.active, v.id, v.effectivedate"
					+ " FROM concept c"
					+ " INNER JOIN version v ON v.id=c.version_id AND c.version_id <> '9999999999'"
					+ " WHERE c.entity_id=?";
		
		PreparedStatement s = _connection.prepareStatement(query);
		s.setLong(1, conceptEntityId);  //TODO with our (bad) h2 copy, this needs to be setSTring, instead of setLong
		
		ResultSet rs = s.executeQuery();
		ArrayList<ConceptImportDTO> results = new ArrayList<>();
		try
		{
			
			while (rs.next()) 
			{
				// Should never be null according to the schema
				Long entity = getLongValue(rs.getLong(1));
				assert entity != null;
				
				ConceptImportDTO ci = new ConceptImportDTO(
												"add", // action
												rs.getString(2), // name
												rs.getString(3), // code
												getLongValue(rs.getLong(4)), // vuid
												rs.getBoolean(5) // active
											);
				ci.setVersion(getLongValue(rs.getLong(6)));
				ci.setTime(getTime(rs, 7));
				results.add(ci);
			}
		}
		finally
		{
			rs.close();
			s.close();
		}
		return results;
	}
	
	
	public Optional<Map<Long, ArrayList<ConceptImportDTO>>> getConcepts()
	{
		return Optional.ofNullable(_concepts);
	} 
	
	
	private void fetchTypes() throws SQLException
	{
		Statement s = _connection.createStatement(ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_READ_ONLY);
		s.setFetchSize(1000);
		
		String query = "SELECT name, kind FROM TYPE";
		
		ResultSet rs = s.executeQuery(query);
		while (rs.next())
		{
			_types.add(new TypeImportDTO(
						rs.getString(2), // kind
						rs.getString(1)) // name
					);
		}
	}
	
	
	public Optional<List<TypeImportDTO>> getTypes()
	{
		return Optional.ofNullable(_types);
	}
	
	
	private void fetchProperties(Optional<Long> conceptEntityId) throws SQLException
	{
		int count = 0;
		
		ResultSet rs = null;
		Statement s = null;
		
		try
		{
			if (conceptEntityId.isPresent())
			{
				String query = "SELECT DISTINCT p.conceptentity_id, t.name, p.property_value, p.active, c.vuid, v.id, v.effectivedate" + " FROM property p"
						+ " INNER JOIN concept c ON c.entity_id=p.conceptentity_id"
						+ " INNER JOIN codesystem cs ON cs.id=c.codesystem_id" 
						+ " INNER JOIN type t ON t.id=p.propertytype_id"
						+ " INNER JOIN version v ON v.id=p.version_id" 
						+ " WHERE p.version_id <> '9999999999'"
						+ " AND p.conceptentity_id=?";
				PreparedStatement ps = _connection.prepareStatement(query);
				ps.setLong(1, conceptEntityId.get());
				rs = ps.executeQuery();
				s = ps;
			}
			else
			{
				printStats(count, 10000, "Properties", false);
				s = _connection.createStatement(ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_READ_ONLY);
				s.setFetchSize(1000);

				String query = "SELECT DISTINCT p.conceptentity_id, t.name, p.property_value, p.active, c.vuid, v.id, v.effectivedate" + " FROM property p"
						+ " INNER JOIN concept c ON c.entity_id=p.conceptentity_id AND c.kind IN ('C','D')"
						+ " INNER JOIN codesystem cs ON cs.id=c.codesystem_id AND cs.name='VHAT'" + " INNER JOIN type t ON t.id=p.propertytype_id"
						+ " INNER JOIN version v ON v.id=p.version_id" + " WHERE p.version_id <> '9999999999'";
				rs = s.executeQuery(query);
			}
			while (rs.next())
			{
				ArrayList<PropertyImportDTO> _al;
				Long entity = getLongValue(rs.getLong(1));
				if (_properties.containsKey(entity))
				{
					_al = _properties.get(entity);
				}
				else
				{
					_al = new ArrayList<PropertyImportDTO>();
					_properties.put(entity, _al);
				}

				ArrayList<PropertyImportDTO> _al2;
				Long vuid = getLongValue(rs.getLong(5)); // vuid
				if (_propertiesByVuid.containsKey(vuid))
				{
					_al2 = _properties.get(entity);
				}
				else
				{
					_al2 = new ArrayList<PropertyImportDTO>();
					_propertiesByVuid.put(vuid, _al2);
				}

				PropertyImportDTO pi = new PropertyImportDTO("add", // action
						rs.getString(2), // typeName
						null, // valueOld
						rs.getString(3), // valueNew
						rs.getBoolean(4) // active
				);
				pi.setVersion(rs.getLong(6));
				pi.setTime(getTime(rs, 7));

				_al.add(pi);
				_al2.add(pi);

				printStats(++count, 1000, "Properties", false);
			} 
		}
		finally
		{
			if (rs != null)
			{
				rs.close();
			}
			if (s != null)
			{
				s.close();
			}
		}
		if (count == 0 && conceptEntityId.isPresent())
		{
			//put in an empty arraylist, so we know we tried to look it up, and don't try again later.
			_properties.put(conceptEntityId.get(), new ArrayList<PropertyImportDTO>());
		}
		
		if (!conceptEntityId.isPresent())
		{
			printStats(count, 1000, "Properties", true);
		}
	}
	
	public Optional<ArrayList<PropertyImportDTO>> getPropertiesForEntity(final Long conceptEntityId)
	{
		Optional<ArrayList<PropertyImportDTO>> temp = Optional.ofNullable(_properties.get(conceptEntityId));
		if (!temp.isPresent())
		{
			try
			{
				fetchProperties(Optional.of(conceptEntityId));
			}
			catch (SQLException e)
			{
				throw new RuntimeException(e);
			}
		}
		return Optional.ofNullable(_properties.get(conceptEntityId));
	}
	
	
	/*public Optional<Map<Long, ArrayList<PropertyImportDTO>>> getPropertiesByVuid()
	{
		return Optional.ofNullable(_propertiesByVuid);
	}*/
	
	
	public Optional<ArrayList<PropertyImportDTO>> getPropertiesForVuid(final Long vuid)
	{
		//return Optional.of(_propertiesByVuid.getOrDefault(vuid, new ArrayList<PropertyImportDTO>()));
		return Optional.ofNullable(_propertiesByVuid.get(vuid));
	}
	
	
	private void fetchRelationships() throws SQLException
	{
		Statement s = _connection.createStatement(ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_READ_ONLY);
		// Be careful with this. As it does improve performance, JDBC
		// pre-allocates an array of this number of rows, requiring more memory
		s.setFetchSize(1000);
		
		String query = "SELECT t.name, c1.code, c2.code, r.active, c1.entity_id, v.id, v.effectivedate"
					+ " FROM relationship r"
					+ " INNER JOIN concept c1 ON c1.entity_id=r.source_entity_id"
					+ " INNER JOIN concept c2 ON c2.entity_id=r.target_entity_id"
					+ " INNER JOIN type t ON r.type_id=t.id"
					+ " INNER JOIN version v on v.id=r.version_id AND v.id <> '9999999999'"
					+ " WHERE r.kind='C'";
		
		int count = 0;
		printStats(count, 10000, "Relationships", false);
		
		ResultSet rs = s.executeQuery(query);
		while (rs.next())
		{
			ArrayList<RelationshipImportDTO> _al = new ArrayList<RelationshipImportDTO>();
			Long entity = getLongValue(rs.getLong(5));
			if (_relationships.containsKey(entity))
			{
				_al = _relationships.get(entity);
			}
			
			RelationshipImportDTO ri = new RelationshipImportDTO(
							"add", // action 
							rs.getString(1), // typeName
							rs.getString(2), // oldTargetCode
							rs.getString(3), // newTargetCode
							rs.getBoolean(4) // active
							);
			ri.setVersion(getLongValue(rs.getLong(6)));
			ri.setTime(getTime(rs, 7));
			_al.add(ri);
			
			_relationships.put(entity, _al);
			
			printStats(++count, 500000, "Relationships", false);
		}
		
		printStats(count, 500000, "Relationships", true);
	}
	
	
	/*public Optional<Map<Long, ArrayList<RelationshipImportDTO>>> getRelationships()
	{
		return Optional.ofNullable(_relationships);
	}*/
	
	
	public Optional<ArrayList<RelationshipImportDTO>> getRelationshipsForEntity(final Long conceptEntityId)
	{
		//Optional<ArrayList<RelationshipImportDTO>> rels = Optional.empty();
		//if (_relationships.containsKey(conceptEntityId))
		//{
			//rels = Optional.ofNullable(_relationships.get(conceptEntityId));
		//}
		//return rels;
		return Optional.ofNullable(_relationships.get(conceptEntityId));
	}
	
	
	public void clearRelationships()
	{
		_relationships = new HashMap<>();
	}
	
	
	@SuppressWarnings("unused")
	private void fetchCodeSystems() throws SQLException
	{
		Statement s = _connection.createStatement(ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_READ_ONLY);
		s.setFetchSize(1000);
		
		// This doesn't account for versions, just a straight fetch from the table 
		String query = "SELECT cs.name, cs.vuid, cs.description, cs.copyright, cs.copyrighturl, t.name as preferred_designation_type"
					+ " FROM CODESYSTEM cs"
					+ " INNER JOIN type t ON t.id=preferred_designation_type_id";
		
		ResultSet rs = s.executeQuery(query);
		while (rs.next())
		{
			_codesystems.add(new CodeSystem(
								rs.getString(1), // codeSystemName 
								getLongValue(rs.getLong(2)), // vuid
								rs.getString(3), // description 
								rs.getString(4), // copyright 
								rs.getString(5), // copyrightURL 
								rs.getString(6), // preferredDesignationType 
								"add"
							));
			// We don't have version in the SQL, or need a way to relate
			//cs.setVersion((Version)elementDataStack.peek().createdChildObjects.remove(0));
		}
	}

	
	public Optional<List<CodeSystem>> getCodeSystems()
	{
		return Optional.ofNullable(_codesystems);
	}

	
	private void fetchMapsets() throws SQLException
	{
		Statement s = _connection.createStatement(ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_READ_ONLY);
		s.setFetchSize(1000);
		
//		String query = "SELECT DISTINCT c.name, c.code, c.vuid, c.active, cs1.name, v1.name, cs2.name, v2.name, c.entity_id, v1.id , v1.effectivedate"
//					+ " FROM concept c"
//					+ " INNER JOIN mapsetextension mse ON mse.mapsetid=c.entity_id"
//					+ " INNER JOIN version v1 ON v1.id=mse.sourceversionid"
//					+ " INNER JOIN version v2 ON v2.id=mse.targetversionid"
//					+ " INNER JOIN codesystem cs1 ON cs1.id=v1.codesystem_id"
//					+ " INNER JOIN codesystem cs2 ON cs2.id=v2.codesystem_id"
//					+ " WHERE c.kind='M'";
		
		// Per Dan on 7/26/17, the VETS group said they are not concerned with
		// the GEM mapsets/mappings and they can be disregarded during the import
		String queryRegexLike = " AND NOT regexp_like(c.name, '^GEM ')";
		if (this._isLocal) // H2
		{
			queryRegexLike = " AND NOT c.name regexp '^GEM '";
		}
		String query = "SELECT DISTINCT c.name, c.code, c.vuid, c.active, cs1.name, v1.name, cs2.name, v2.name, c.entity_id, v3.id , v3.effectivedate, v1.id, v2.id"
					+ " FROM concept c"
					+ " INNER JOIN mapsetextension mse ON mse.mapsetid=c.entity_id"
					+ " INNER JOIN version v1 ON v1.id=mse.sourceversionid"
					+ " INNER JOIN version v2 ON v2.id=mse.targetversionid"
					+ " INNER JOIN version v3 ON v3.id=c.version_id AND v3.id  <> '9999999999'"
					+ " INNER JOIN codesystem cs1 ON cs1.id=v1.codesystem_id"
					+ " INNER JOIN codesystem cs2 ON cs2.id=v2.codesystem_id"
					+ " WHERE c.kind='M'"
					+ queryRegexLike
					;
				
		int count = 0;
		printStats(count, 10000, "MapSets", false);
		
		ResultSet rs = s.executeQuery(query);
		while (rs.next())
		{
			ArrayList<MapSetImportDTO> _al = new ArrayList<MapSetImportDTO>();
			Long entity = getLongValue(rs.getLong(9));
			if (_mapsets.containsKey(entity))
			{
				_al = _mapsets.get(entity);
			}
			
			MapSetImportDTO msi = new MapSetImportDTO(
										"add", // action 
										rs.getString(1), // name 
										rs.getString(2), // code
										getLongValue(rs.getLong(3)), // vuid 
										rs.getBoolean(4), // active 
										rs.getString(5), // sourceCodeSystemName 
										rs.getString(6), // sourceVersionName 
										rs.getString(7), // targetCodeSystemName 
										rs.getString(8) // targetVersionName
									);
			msi.setVersion(getLongValue(rs.getLong(10)));
			msi.setTime(getTime(rs, 11));
			msi.setSourceVersionId(getLongValue(rs.getLong(12)));
			msi.setTargetVersionId(getLongValue(rs.getLong(13)));
			_al.add(msi);
			
			_mapsets.put(entity, _al);
			
			printStats(++count, 10000, "MapSets", false);
		}
		
		printStats(count, 10000, "MapSets", true);
	}
	
	
	public Optional<List<MapSetImportDTO>> getFullListOfMapSets()
	{
		List<MapSetImportDTO> returnList = new ArrayList<>();
		_mapsets.values().forEach((al) -> {
			returnList.addAll(al);
		});
		return Optional.ofNullable(returnList);
	}
	
	
	public Optional<Map<Long, ArrayList<MapSetImportDTO>>> getMapSets()
	{
		return Optional.ofNullable(_mapsets);
	}

	
	private void fetchSubsets() throws SQLException
	{
		Statement s = _connection.createStatement(ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_READ_ONLY);
		s.setFetchSize(1000);
		
		String query = "SELECT c.vuid, c.name, c.active, c.entity_id, v.id, v.effectivedate"
					+ " FROM concept c"
					+ " INNER JOIN version v on v.id=c.version_id AND v.id != '9999999999'"
					+ " WHERE kind='S' AND c.version_id <> '9999999999'";
		
		int count = 0;
		printStats(count, 10000, "Subsets", false);
		
		ResultSet rs = s.executeQuery(query);
		while (rs.next())
		{
			ArrayList<SubsetImportDTO> _al = new ArrayList<SubsetImportDTO>();
			Long entity = getLongValue(rs.getLong(4));
			if (_subsets.containsKey(entity))
			{
				_al = _subsets.get(entity);
			}
			
			Long vuid = getLongValue(rs.getLong(1)); // vuid
			SubsetImportDTO si = new SubsetImportDTO(
					"add", // action
					vuid,
					rs.getString(2), // subsetName
					rs.getBoolean(3) // active
					);
			si.setVersion(getLongValue(rs.getLong(5)));
			si.setTime(getTime(rs, 6));
			_al.add(si);
			
			_subsets.put(entity, _al);
			_subsetsByVuid.put(vuid, rs.getString(2));
			
			printStats(++count, 10000, "Subsets", false);
		}
		
		printStats(count, 10000, "Subsets", true);
	}
	
	
	public Optional<List<SubsetImportDTO>> getFullListOfSubsets()
	{
		List<SubsetImportDTO> returnList = new ArrayList<>();
		_subsets.values().forEach((al) -> {
			returnList.addAll(al);
		});
		return Optional.ofNullable(returnList);
	}

	
	public Optional<Map<Long, ArrayList<SubsetImportDTO>>> getSubsets()
	{
		return Optional.ofNullable(_subsets);
	}
	
	
	public Optional<Map<Long, String>> getSubsetsByVuid()
	{
		return Optional.ofNullable(_subsetsByVuid);
	}
	
	
	private void fetchSubsetMemberships() throws SQLException
	{
		Statement s = _connection.createStatement(ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_READ_ONLY);
		s.setFetchSize(1000);
		
		String query = "SELECT DISTINCT c1.name, c1.vuid, c2.name, c2.vuid, r.active, c2.entity_id, c2.code , v.id, v.effectivedate"
					+ " FROM relationship r"
					+ " INNER JOIN concept c1 ON c1.entity_id=r.source_entity_id"
					+ " INNER JOIN concept c2 ON c2.entity_id=r.target_entity_id"
					+ " INNER JOIN version v ON v.id=r.version_id AND v.id <> '9999999999'"
					+ " WHERE r.kind='S'";

		int count = 0;
		printStats(count, 10000, "SubsetMemberships", false);
		
		ResultSet rs = s.executeQuery(query);
		while (rs.next())
		{
			Long entity = getLongValue(rs.getLong(6));
			Long subsetVuid = getLongValue(rs.getLong(2));
			Long designationCode = getLongValue(rs.getLong(7));
			
			ArrayList<SubsetMembershipImportDTO> _al = new ArrayList<SubsetMembershipImportDTO>();
			if (_subsetmemberships.containsKey(entity))
			{
				_al = _subsetmemberships.get(entity);
			}
			
			SubsetMembershipImportDTO smi = new SubsetMembershipImportDTO(
					"add", // action 
					subsetVuid, // vuid 
					rs.getBoolean(5) // active
				);
			smi.setVersion(rs.getLong(8));
			smi.setTime(getTime(rs, 9));
			_al.add(smi);
//			_al.add(new SubsetMembershipImportDTO(
//					"add", // action 
//					subsetVuid, // vuid 
//					rs.getBoolean(5) // active
//				));
			_subsetmemberships.put(entity, _al);
		
			ArrayList<Long> _al2 = new ArrayList<Long>();
			if (_subsetmembershipsByVuid.containsKey(subsetVuid))
			{
				_al2 = _subsetmembershipsByVuid.get(subsetVuid);
			}
			
			_al2.add(designationCode);
			_subsetmembershipsByVuid.put(subsetVuid, _al2);
			
			printStats(++count, 10000, "SubsetMemberships", false);
		}
		
		printStats(count, 10000, "SubsetMemberships", true);
	}
	
	
	public Map<Long, ArrayList<SubsetMembershipImportDTO>> getSubsetMembershipsMap()
	{
		return _subsetmemberships;
	}
	
	public Map<Long, ArrayList<Long>> getSubsetMembershipsCodesByVuid()
	{
		return _subsetmembershipsByVuid;
	}
	
	
	private void fetchMapEntries() throws SQLException
	{
		Statement s = _connection.createStatement(ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_READ_ONLY);
		s.setFetchSize(1000);
		
		String query = "SELECT DISTINCT c1.entity_id, c2.entity_id, c2.vuid, r.active, mee.sourcecode, mee.targetcode, r.sequence, r.grouping, mee.effectivedate, c1.vuid, v.id, v.effectivedate"
					+ " FROM relationship r"
					+ " INNER JOIN concept c1 ON c1.entity_id=r.source_entity_id"
					+ " INNER JOIN concept c2 ON c2.entity_id=r.target_entity_id"
					+ " INNER JOIN mapentryextension mee ON mee.mapentryid=c2.entity_id"
					+ " INNER JOIN version v on v.id=r.version_id AND v.id <> '9999999999'"
					+ " WHERE r.kind='M'";
		
		int count = 0;
		printStats(count, 10000, "MapEntries", false);
		
		ResultSet rs = s.executeQuery(query);
		while (rs.next())
		{
			ArrayList<MapEntryImportDTO> _al = new ArrayList<MapEntryImportDTO>();
			// Key'd by the parent MapSet VUID for the Mojo
			Long mapsetVuid = getLongValue(rs.getLong(10));
			Integer sequence = getIntegerValue(rs.getInt(7));

			if (_mapentries.containsKey(mapsetVuid))
			{
				_al = _mapentries.get(mapsetVuid);
			}
			
			MapEntryImportDTO mei = new MapEntryImportDTO(
									"add", // action 
									getLongValue(rs.getLong(3)), // vuid 
									rs.getBoolean(4), // active 
									rs.getString(5), // sourceCode 
									rs.getString(6), // targetCode 
									sequence == null ? 0 : sequence.intValue(), // sequence 
									getLongValue(rs.getLong(8)), // grouping, 
									getDateObject(rs.getString(9)).orElse(null) // effectiveDate
								);
			mei.setVersion(getLongValue(rs.getLong(11)));
			mei.setTime(getTime(rs, 12));

			_al.add(mei);
			
			_mapentries.put(mapsetVuid, _al);
			
			printStats(++count, 100000, "MapEntries", false);
		}
		
		printStats(count, 100000, "MapEntries", true);
	}
	
	
	public Optional<ArrayList<MapEntryImportDTO>> getMapEntriesForMapSet(final Long vuid)
	{
		return Optional.ofNullable(_mapentries.get(vuid));
	}
	
	
	private void fetchDesignations(Optional<Long> conceptEntityId) throws SQLException
	{
		ResultSet rs = null;
		Statement s = null;
		
		int count = 0;
		try
		{
			if (conceptEntityId.isPresent())
			{
				String query = "SELECT DISTINCT t.name as type_name, c1.code, c1.vuid, c1.name as value, c1.active, v.name, c2.entity_id, v.id, v.effectivedate"
						+ " FROM relationship r" 
						+ " INNER JOIN concept c1 ON c1.entity_id=r.target_entity_id"
						+ " INNER JOIN concept c2 ON c2.entity_id=r.source_entity_id" 
						+ " INNER JOIN type t ON t.id=c1.type_id"
						+ " INNER JOIN version v ON v.id=r.version_id AND v.id <> '9999999999'" 
						+ " WHERE r.kind='D'"
						+ " AND r.source_entity_id=?";

				PreparedStatement ps = _connection.prepareStatement(query);
				ps.setLong(1, conceptEntityId.get());  //TODO with our (bad) h2 copy, this needs to be setSTring, instead of setLong
				s = ps;
				rs = ps.executeQuery();
			}
			else
			{
				printStats(count, 10000, "Designations", false);
				s = _connection.createStatement(ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_READ_ONLY);
				s.setFetchSize(1000);

				String query = "SELECT DISTINCT t.name as type_name, c1.code, c1.vuid, c1.name as value, c1.active, v.name, c2.entity_id, v.id, v.effectivedate"
						+ " FROM relationship r" + " INNER JOIN concept c1 ON c1.codesystem_id='1' AND c1.entity_id=r.target_entity_id"
						+ " INNER JOIN concept c2 ON c2.entity_id=r.source_entity_id AND c2.kind='C'" + " INNER JOIN type t ON t.id=c1.type_id"
						+ " INNER JOIN version v ON v.id=r.version_id AND v.id <> '9999999999'" + " WHERE r.kind='D'";

				rs = s.executeQuery(query);
			}
			
			while (rs.next())
			{
				ArrayList<DesignationExtendedImportDTO> al;
				Long entity = getLongValue(rs.getLong(7));
				if (_designations.containsKey(entity))
				{
					al = _designations.get(entity);
				}
				else
				{
					al = new ArrayList<DesignationExtendedImportDTO>();
					_designations.put(entity, al);
				}

				DesignationExtendedImportDTO dei = new DesignationExtendedImportDTO("add", // action 
						rs.getString(1), // typeName 
						rs.getString(2), // code 
						null, // valueOld 
						rs.getString(4), // valueNew 
						getLongValue(rs.getLong(3)), // vuid 
						null, // moveFromConceptCode
						rs.getBoolean(5) // active
				);
				dei.setVersion(getLongValue(rs.getLong(8)));
				dei.setTime(getTime(rs, 9));
				al.add(dei);
				
				if (conceptEntityId.isPresent() && !getPropertiesForVuid(dei.getVuid()).isPresent() && !getPropertiesForEntity(entity).isPresent())
				{
					//populate the cache of designation properties, that may have otherwise been missed (non VHAT things)
					fetchProperties(Optional.of(entity));
				}

				printStats(++count, 10000, "Designations", false);
			}
			if (count == 0 && conceptEntityId.isPresent())
			{
				//Put in a marker, so we don't requery again later if there aren't any.
				_designations.put(conceptEntityId.get(), new ArrayList<DesignationExtendedImportDTO>());
			}
		}
		finally
		{
			if (rs != null)
			{
				rs.close();
			}
			if (s != null)
			{
				s.close();
			}
		}
		if (!conceptEntityId.isPresent())
		{
			printStats(count, 10000, "Designations", true);
		}
	}
	
	
	private void fetchMapSetDesignations() throws SQLException
	{
		Statement s = _connection.createStatement(ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_READ_ONLY);
		s.setFetchSize(1000);
		
//		String query = "SELECT t.name, c1.code, c1.vuid, c1.name, c1.active, v.id, v.effectivedate, r.source_entity_id"
//					+ " FROM relationship r"
//					+ " INNER JOIN concept c1 ON c1.codesystem_id='1' AND c1.entity_id=r.target_entity_id"
//					+ " INNER JOIN type t ON t.id=c1.type_id"
//					+ " INNER JOIN version v ON v.id=r.version_id AND v.id != '9999999999'"
//					+ " WHERE r.kind='D'"
//					+ " AND r.source_entity_id IN (" // Mostly a copy from the Mapset query 
//					+ "		SELECT c.entity_id"
//					+ "		FROM concept c"
//					+ "		INNER JOIN mapsetextension mse ON mse.mapsetid=c.entity_id"
//					+ "		INNER JOIN version v1 ON v1.id=mse.sourceversionid"
//					+ "		INNER JOIN version v2 ON v2.id=mse.targetversionid"
//					+ "		INNER JOIN codesystem cs1 ON cs1.id=v1.codesystem_id"
//					+ "		INNER JOIN codesystem cs2 ON cs2.id=v2.codesystem_id"
//					+ "		WHERE c.kind='M'"
//					+ ")";
		
		String query = "SELECT DISTINCT t.name, c1.code, c1.vuid, c1.name, "
					+ " (SELECT c.active FROM concept c WHERE c.entity_id=c2.entity_id AND c.version_id=v.id) AS \"active\","
					+ " v.id, v.effectivedate, r.source_entity_id"
					+ " FROM relationship r"
					+ " INNER JOIN codesystem cs1 on cs1.name='VHAT'"
					+ " INNER JOIN codesystem cs2 on cs2.name='VHAT'"
					+ " INNER JOIN concept c1 ON c1.entity_id=r.target_entity_id AND c1.codesystem_id=cs1.id"
					+ " INNER JOIN concept c2 ON c2.entity_id=r.source_entity_id AND c2.codesystem_id=cs2.id"
					+ " INNER JOIN type t ON t.id=c1.type_id"
					+ " INNER JOIN version v ON v.id=r.version_id AND v.id <> '9999999999'"
					+ " WHERE r.kind='D'"
					+ " AND r.source_entity_id IN ("
					+ " 		SELECT DISTINCT c.entity_id"
					+ " 		FROM concept c"
					+ " 		INNER JOIN mapsetextension mse ON mse.mapsetid=c.entity_id"
					+ " 		INNER JOIN version v1 ON v1.id=mse.sourceversionid"
					+ " 		INNER JOIN version v2 ON v2.id=mse.targetversionid"
					+ " 		INNER JOIN codesystem cs1 ON cs1.id=v1.codesystem_id"
					+ " 		INNER JOIN codesystem cs2 ON cs2.id=v2.codesystem_id"
					+ " 		WHERE c.kind='M'"
					+ ")";
		
		int count = 0;
		printStats(count, 10000, "MapSetDesignations", false);
		
		ResultSet rs = s.executeQuery(query);
		while (rs.next())
		{
			ArrayList<DesignationExtendedImportDTO> _al = new ArrayList<DesignationExtendedImportDTO>();
			Long entity = getLongValue(rs.getLong(8));
			if (_designations.containsKey(entity))
			{
				_al = _designations.get(entity);
			}
			
			DesignationExtendedImportDTO dei = new DesignationExtendedImportDTO(
									"add", // action 
									rs.getString(1), // typeName 
									rs.getString(2), // code 
									null, // valueOld 
									rs.getString(4), // valueNew 
									getLongValue(rs.getLong(3)), // vuid 
									null, // moveFromConceptCode
									rs.getBoolean(5) // active
								);
			dei.setVersion(getLongValue(rs.getLong(6)));
			dei.setTime(getTime(rs, 7));
			_al.add(dei);
			
			_designations.put(entity, _al);
				
			printStats(++count, 10, "MapSetDesignations", false);
		}
		
		printStats(count, 10, "MapSetDesignations", true);
	}

	
	public Optional<Map<Long, ArrayList<DesignationExtendedImportDTO>>> getDesignations()
	{
		return Optional.ofNullable(_designations);
	}
	
	
	public Optional<ArrayList<DesignationExtendedImportDTO>> getDesignationsForEntity(final Long conceptEntityId)
	{
		Optional<ArrayList<DesignationExtendedImportDTO>> temp = Optional.ofNullable(_designations.get(conceptEntityId));
		if (!temp.isPresent())
		{
			try
			{
				fetchDesignations(Optional.of(conceptEntityId));
			}
			catch (SQLException e)
			{
				throw new RuntimeException(e);
			}
		}
		return Optional.ofNullable(_designations.get(conceptEntityId));
	}
	
	
	private Integer getIntegerValue(int value)
	{
		// ResultSet getInt() guaranteed to be not null
		// but we'll return null for any '0' values
		Integer result = null;
		
		if (value > 0)
		{
			result = Integer.valueOf(value);
		}
		
		return result;
	}
	
	
	private Long getLongValue(long value)
	{
		// ResultSet getLong() guaranteed to be not null
		// but we'll return null for any '0' values
		Long result = null;
		
		if (value > 0)
		{
			result = Long.valueOf(value);
		}
		
		return result;
	}
	
	
	private Date getDate(ResultSet rs, int columnNum) throws SQLException
	{
		if (_isLocal)
		{
			return getDateObject(rs.getString(columnNum)).orElse(null);
		}
		
		return getDateObject(rs.getTimestamp(columnNum)).orElse(null);
	}
	
	
	private Optional<Date> getDateObject(String d)
	{
		Date dt = null;
		if (d != null)
		{
			try
			{
				// TODO: Using UTC for the moment, need to review
				dt = Date.from(LocalDateTime.parse(d, _dateTimeFormatter)
						.toInstant(ZoneOffset.UTC));
			}
			catch (Exception e)
			{
				// TODO
				System.out.println(e);
			}
		}
		return Optional.ofNullable(dt);
	}

	
	private Optional<Date> getDateObject(java.sql.Timestamp ts)
	{
		return Optional.ofNullable(ts);
	}

	
	private Long getTimeFromDate(String d)
	{
		Long time = null;
		if (getDateObject(d).isPresent())
		{
			time = Long.valueOf(getDateObject(d).get().getTime());
		}
		return time;
	}
	
	
	private Long getTimeFromTimestamp(java.sql.Timestamp ts)
	{
		Long time = null;
		if (ts != null)
		{
			time = ts.getTime();
		}
		return time;
	}
	
	
	private Long getTime(ResultSet rs, int columnNum) throws SQLException
	{
		Long t = null;
		
		if (_isLocal)
		{
			t = getTimeFromDate(rs.getString(columnNum));
		}
		else
		{
			t = getTimeFromTimestamp(rs.getTimestamp(columnNum));
		}
		
		return t;
	}
	
	
	private void printStats(int count, int interval, String message, boolean finalCount)
	{
		if (count == 0)
		{
			log.info(message + " : Starting.");
		} 
		else if ((count % interval) == 0)
		{
			log.info(message + " : Fetched " + count + " rows.");
		} 
		else if (finalCount)
		{
			log.info(message + " : Finished with " + count + " rows.");
		}
	}
	
	
	private Map<String, String> readProperties(Properties props, String env) throws IOException
	{
		Map<String, String> envProps = new HashMap<>();
		envProps.put("driver", props.getProperty(env+".jdbc.driver"));
		envProps.put("url", props.getProperty(env+".jdbc.url"));
		envProps.put("username", props.getProperty(env+".jdbc.username"));
		envProps.put("password", props.getProperty(env+".jdbc.password"));
		
		return envProps;
	}
}
