/**
 * END USER LICENSE AGREEMENT (“EULA”)
 *
 * READ THIS AGREEMENT CAREFULLY (date: 9/13/2011):
 * http://www.akiban.com/licensing/20110913
 *
 * BY INSTALLING OR USING ALL OR ANY PORTION OF THE SOFTWARE, YOU ARE ACCEPTING
 * ALL OF THE TERMS AND CONDITIONS OF THIS AGREEMENT. YOU AGREE THAT THIS
 * AGREEMENT IS ENFORCEABLE LIKE ANY WRITTEN AGREEMENT SIGNED BY YOU.
 *
 * IF YOU HAVE PAID A LICENSE FEE FOR USE OF THE SOFTWARE AND DO NOT AGREE TO
 * THESE TERMS, YOU MAY RETURN THE SOFTWARE FOR A FULL REFUND PROVIDED YOU (A) DO
 * NOT USE THE SOFTWARE AND (B) RETURN THE SOFTWARE WITHIN THIRTY (30) DAYS OF
 * YOUR INITIAL PURCHASE.
 *
 * IF YOU WISH TO USE THE SOFTWARE AS AN EMPLOYEE, CONTRACTOR, OR AGENT OF A
 * CORPORATION, PARTNERSHIP OR SIMILAR ENTITY, THEN YOU MUST BE AUTHORIZED TO SIGN
 * FOR AND BIND THE ENTITY IN ORDER TO ACCEPT THE TERMS OF THIS AGREEMENT. THE
 * LICENSES GRANTED UNDER THIS AGREEMENT ARE EXPRESSLY CONDITIONED UPON ACCEPTANCE
 * BY SUCH AUTHORIZED PERSONNEL.
 *
 * IF YOU HAVE ENTERED INTO A SEPARATE WRITTEN LICENSE AGREEMENT WITH AKIBAN FOR
 * USE OF THE SOFTWARE, THE TERMS AND CONDITIONS OF SUCH OTHER AGREEMENT SHALL
 * PREVAIL OVER ANY CONFLICTING TERMS OR CONDITIONS IN THIS AGREEMENT.
 */
package com.akiban.qp.persistitadapter.indexrow;

import com.akiban.ais.model.IndexToHKey;
import com.akiban.ais.model.TableIndex;
import com.akiban.ais.model.UserTable;
import com.akiban.qp.persistitadapter.PersistitAdapter;
import com.akiban.qp.persistitadapter.PersistitHKey;
import com.akiban.qp.row.HKey;
import com.akiban.qp.rowtype.IndexRowType;
import com.persistit.exception.PersistitException;

public class PersistitTableIndexRow extends PersistitIndexRow
{
    // RowBase interface

    @Override
    public HKey ancestorHKey(UserTable table)
    {
        PersistitHKey ancestorHKey;
        PersistitHKey leafmostHKey = hKeyCache.hKey(leafmostTable);
        if (table == leafmostTable) {
            ancestorHKey = leafmostHKey;
        } else {
            ancestorHKey = hKeyCache.hKey(table);
            leafmostHKey.copyTo(ancestorHKey);
            ancestorHKey.useSegments(table.getDepth() + 1);
        }
        return ancestorHKey;
    }

    // PersistitIndexRow interface

    @Override
    public IndexToHKey indexToHKey()
    {
        return index.indexToHKey();
    }

    // PersistitTableIndexRow interface

    public PersistitTableIndexRow(PersistitAdapter adapter, IndexRowType indexRowType)
    {
        super(adapter, indexRowType);
        this.index = (TableIndex) indexRowType.index();
    }

    // Object state

    private final TableIndex index;
}