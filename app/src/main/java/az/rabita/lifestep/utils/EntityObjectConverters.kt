package az.rabita.lifestep.utils

import az.rabita.lifestep.pojo.apiPOJO.content.*
import az.rabita.lifestep.pojo.apiPOJO.entity.*

fun ContentContentPOJO.asContentEntityObject(groupId: Int) = Content(
    id = id,
    contentKey = transKey,
    content = text,
    imageUrl = url,
    groupID = groupId
)

fun List<ContentContentPOJO>.asContentEntityObject(groupId: Int) = map {
    Content(
        id = it.id,
        contentKey = it.transKey,
        content = it.text,
        imageUrl = it.url,
        groupID = groupId
    )
}

fun List<FriendContentPOJO>.asFriendEntityObject() = map {
    Friend(
        id = it.id,
        surname = it.fullName.split(" ")[0],
        name = it.fullName.split(" ")[1],
        balance = it.balance
    )
}

fun List<CategoriesContentPOJO>.asCategoryEntityObject() = map {
    Category(
        id = it.id,
        name = it.name,
        url = it.url,
        isReady = it.isReady
    )
}

fun List<AssocationContentPOJO>.asAssocationEntityObject() = map {
    Assocation(
        id = it.id,
        name = it.name,
        stepNeed = it.stepNeed.moreShortenString(),
        balance = it.balance.moreShortenString(),
        percent = it.percent,
        url = it.url
    )
}

fun List<WalletContentPOJO>.asWalletEntityObject() = map {
    Wallet(
        id = 1,
        balance = it.balance.toString(),
        balanceMoney = it.balanceMoney.toString(),
        bonusSteps = it.bonusSteps?.littleShortenString(),
        convertSteps = it.convertSteps?.littleShortenString(),
        donationSteps = it.donationSteps?.littleShortenString(),
        transferStepsIn = it.transferStepsIn?.littleShortenString(),
        transferStepsOut = it.transferStepsOut?.littleShortenString()
    )
}

fun List<FriendshipContentPOJO>.asFriendshipStatsEntityObject() = map {
    FriendshipStats(
        id = 1,
        friendsCount = it.friendsCount,
        requestCount = it.requestCount
    )
}

fun List<PersonalInfoContentPOJO>.asPersonalInfoEntityObject() = map {
    PersonalInfo(
        saveId = 1,
        id = it.id,
        name = it.name,
        phone = it.phone,
        invitationCode = it.invitationCode,
        fullName = it.fullName,
        email = it.email,
        url = it.url,
        originalImageUrl = it.originalUrl,
        surname = it.surname,
        createdDate = it.createdDate,
        balance = it.balance,
        friendsCount = it.friendsCount
    )
}

fun List<OwnProfileInfoContentPOJO>.asOwnProfileInfoEntityObject() = map {
    PersonalInfo(
        saveId = 1,
        id = it.id,
        name = it.name,
        phone = it.phone,
        invitationCode = it.invitationCode,
        fullName = it.fullName,
        email = it.email,
        url = it.url,
        originalImageUrl = it.originalUrl,
        surname = it.surname,
        createdDate = it.createdDate,
        balance = it.balance,
        friendsCount = it.friendsCount
    )
}

fun List<DailyContentPOJO>.asDailyStatsEntityObject() = map {
    DailyStat(
        createdDate = it.createdDate,
        count = it.count,
        shortName = it.shortName
    )
}

fun List<MonthlyContentPOJO>.asMonthlyStatsEntityObject() = map {
    MonthlyStat(
        month = it.month,
        count = it.count,
        monthName = it.monthName
    )
}

fun List<NotificationContentPOJO>.asNotificationEntityObject() = map {
    Notifications(
        id = it.id,
        imageUrl = it.imageUrl,
        messages = it.messages,
        title = it.title,
        url = it.url,
        usersId = it.usersId,
        usersNotificationsTypesId = it.usersNotificationsTypesId
    )
}

fun List<WeeklyContentPOJO>.asWeeklyStatsEntityObject() = map {
    WeeklyStat(
        date = it.date,
        calories = it.calories,
        convertedSteps = it.convertedSteps,
        isSelected = it.isSelected,
        kilometers = it.kilometers,
        stepCount = it.stepCount,
        unconvertedSteps = it.unconvertedSteps,
        weekName = it.weekName
    )
}