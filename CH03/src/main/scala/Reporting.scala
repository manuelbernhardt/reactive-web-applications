import org.joda.time.DateTime

case class Click(timestamp: DateTime, advertisementId: Long)

case class Month(year: Int, month: Int)

trait ClickRepository {

  def getClicksSince(when: DateTime): List[Click]
}

object Reporting {

  def computeYearlyAggregates(clickRepository: ClickRepository): Map[Long, Seq[(Month, Int)]] = {
    val pastClicks = clickRepository.getClicksSince(DateTime.now.minusYears(1))
    pastClicks.groupBy(_.advertisementId).mapValues {
      case clicks =>
        val monthlyClicks = clicks
          .groupBy(click => Month(click.timestamp.getYear, click.timestamp.getMonthOfYear))
          .map { case (month, groupedClicks) =>
          month -> groupedClicks.length
        }.toSeq
        monthlyClicks
    }
  }

  def computeYearlyAggregatesRefactored(clickRepository: ClickRepository): Map[Long, Seq[(Month, Int)]] = {

    def monthOfClick(click: Click) = Month(click.timestamp.getYear, click.timestamp.getMonthOfYear)

    def countMonthlyClicks(monthlyClicks: (Month, Seq[Click])) = monthlyClicks match {
      case (month, clicks) =>
        month -> clicks.length
    }

    def computeMonthlyAggregates(clicks: Seq[Click]) = clicks.groupBy(monthOfClick).map(countMonthlyClicks).toSeq

    val pastClicks = clickRepository.getClicksSince(DateTime.now.minusYears(1))

    pastClicks.groupBy(_.advertisementId).mapValues(computeMonthlyAggregates)

  }

}
