/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package gov.vha.isaac.ochre.api.commit;

import org.jvnet.hk2.annotations.Contract;

/**
 *
 * @author kec
 * @deprecated to make naming more consistent. Use CommitService instead of CommitManager
 */
@Deprecated
@Contract
public interface CommitManager extends CommitService {

}
