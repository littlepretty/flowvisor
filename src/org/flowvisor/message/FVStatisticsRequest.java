package org.flowvisor.message;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import org.flowvisor.classifier.FVClassifier;
import org.flowvisor.exceptions.StatDisallowedException;
import org.flowvisor.log.FVLog;
import org.flowvisor.log.LogLevel;
import org.flowvisor.message.statistics.SlicableStatistic;
import org.flowvisor.slicer.FVSlicer;
import org.openflow.protocol.OFStatisticsMessageBase;
import org.openflow.protocol.OFStatisticsRequest;
import org.openflow.protocol.statistics.OFStatistics;

public class FVStatisticsRequest extends OFStatisticsRequest implements
		Classifiable, Slicable, SanityCheckable {

	@Override
	public void classifyFromSwitch(FVClassifier fvClassifier) {
		FVLog.log(LogLevel.WARN, fvClassifier, "dropping unexpected msg: "
				+ this);
	}

	@Override
	public void sliceFromController(FVClassifier fvClassifier, FVSlicer fvSlicer) {
		boolean hasBody = false;
		List<OFStatistics> newStatsList = new LinkedList<OFStatistics>();
		Iterator<OFStatistics> it = this.getStatistics().iterator();
		while (it.hasNext()) {
			OFStatistics stat = it.next();
			assert (stat instanceof SlicableStatistic);
			try {
				((SlicableStatistic) stat).sliceFromController(newStatsList, fvClassifier,
						fvSlicer);
				hasBody = true;
			} catch (StatDisallowedException e) {
				it.remove();
				this.setLengthU(this.getLengthU() - stat.getLength());
				FVLog.log(LogLevel.WARN, fvSlicer, e.getMessage());
				fvSlicer.sendMsg(FVMessageUtil.makeErrorMsg(
						e.getError(), this), fvSlicer);
			}
			
		}
	
		if (!hasBody)
			// else just slice by xid and hope for the best
			FVMessageUtil.translateXidAndSend(this, fvClassifier, fvSlicer);
		else {
			this.setLengthU(OFStatisticsMessageBase.MINIMUM_LENGTH);
			for (OFStatistics stat : newStatsList) {
				this.setLengthU(this.getLengthU() + stat.getLength());
				this.setStatistics(newStatsList);
			}
			FVMessageUtil.translateXidAndSend(this, fvClassifier, fvSlicer);
		}
	
	}

	@Override
	public String toString() {
		return super.toString() + ";st=" + this.getStatisticType();
		// ";mfr=" + this.getManufacturerDescription() +
	}

	/**
	 * Check to make sure the packet really has all of the statistics is claims
	 * to have. This is really to make sure the framing is correct.
	 */
	@Override
	public boolean isSane() {
		int msgLen = this.getLengthU();
		int count;
		count = OFStatisticsMessageBase.MINIMUM_LENGTH;
		for (OFStatistics stat : this.getStatistics()) {
			count += stat.getLength();
		}
		if (count == msgLen)
			return true;
		else {
			FVLog.log(LogLevel.WARN, null, "msg failed sanity check: " + this);
			return false;
		}
	}

}
