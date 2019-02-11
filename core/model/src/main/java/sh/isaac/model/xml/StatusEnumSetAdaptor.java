/*
 * Copyright 2018 Organizations participating in ISAAC, ISAAC's KOMET, and SOLOR development include the
         US Veterans Health Administration, OSHERA, and the Health Services Platform Consortium..
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package sh.isaac.model.xml;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;
import javax.xml.bind.annotation.adapters.XmlAdapter;
import sh.isaac.api.Status;

/**
 *
 * @author kec
 */
public class StatusEnumSetAdaptor extends XmlAdapter<StatusEnumSetAdaptor.StatusSet, EnumSet<Status>> {

    @XmlType(name = "StatusSet")
    public static class StatusSet {
        @XmlElement(name = "StatusList")
        List<Status> statusList = new ArrayList();
    }

    @Override
    public StatusSet marshal(EnumSet<Status> v) throws Exception {
        StatusSet set = new StatusSet();
        set.statusList.addAll(v);
        return set;
    }

    @Override
    public EnumSet<Status> unmarshal(StatusSet v) throws Exception {
        EnumSet<Status> e = EnumSet.noneOf(Status.class);
        e.addAll(v.statusList);
        return e;
    }

}
