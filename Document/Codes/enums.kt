for (cellInfo in cellInfoList) {
            when (cellInfo) {
                is CellInfoGsm -> {
                    val cellIdentityGsm = cellInfo.cellIdentity
                    val cellSignalStrengthGsm = cellInfo.cellSignalStrength
                    val info = "0:${cellIdentityGsm.cid},${cellSignalStrengthGsm.dbm};"
                    cellInfoStr.append(info)
                    Log.i(TAG, info)
                }
                is CellInfoCdma -> {
                    val cellIdentityCdma = cellInfo.cellIdentity
                    val cellSignalStrengthCdma = cellInfo.cellSignalStrength
                    val info = "1:${cellIdentityCdma.basestationId},${cellSignalStrengthCdma.dbm};"
                    cellInfoStr.append(info)
                    Log.i(TAG, info)
                }
                is CellInfoLte -> {
                    val cellIdentityLte = cellInfo.cellIdentity
                    val cellSignalStrengthLte = cellInfo.cellSignalStrength
                    val info = "2:${cellIdentityLte.ci},${cellSignalStrengthLte.dbm};"
                    cellInfoStr.append(info)
                    Log.i(TAG, info)
                }
                is CellInfoWcdma -> {
                    val cellIdentityWcdma = cellInfo.cellIdentity
                    val cellSignalStrengthWcdma = cellInfo.cellSignalStrength
                    val info = "3:${cellIdentityWcdma.cid},${cellSignalStrengthWcdma.dbm};"
                    cellInfoStr.append(info)
                    Log.i(TAG, info)
                }
                else -> {
                    val info = "Unknown Cell Type\n"
                    cellInfoStr.append(info)
                    Log.i(TAG, info)
                }
            }
        }