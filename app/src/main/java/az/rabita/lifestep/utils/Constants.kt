package az.rabita.lifestep.utils

import com.google.android.gms.fitness.FitnessOptions
import com.google.android.gms.fitness.data.DataType

//TAGS
const val ERROR_TAG = "error"
const val LOADING_TAG = "loading"

//KEYS
const val TOKEN_KEY = "token"
const val LANG_KEY = "lang"
const val MAIN_TO_FORGOT_PASSWORD_KEY = "fromEditProfilePage"
const val TITLE_KEY = "title"
const val CONTENT_TEXT_KEY = "contenttext"
const val INVITE_TEXT_KEY = "damnKey"
const val LANG_AZ_KEY = "az"
const val LANG_EN_KEY = "en"
const val LANG_RU_KEY = "ru"
const val DEFAULT_LANG_KEY = "az"
const val POST_KEY = "postId"
const val PLAYER_ID_KEY = "playerId"
const val STATIC_TOKEN = "4019a465-a6ec-4ec8-8928-e668ec34c947"

//ID
const val ADS_GROUP_ID = 30
const val CONTACTS_GROUP_ID = 40
const val ABOUT_US_GROUP_ID = 50
const val INVITE_FRIENDS_GROUP_ID = 60

const val AD_REWARDED_ID = "ca-app-pub-3940256099942544/5224354917"

//CONSTANT VALUES
const val NETWORK_PAGE_SIZE = 5
const val GOOGLE_FIT_PERMISSIONS_REQUEST_CODE = 1
const val ACTIVITY_RECOGNITION_REQUEST_CODE = 2
const val LANG_AZ = 10
const val LANG_EN = 20
const val LANG_RU = 30
const val DEFAULT_LANG = 10

val langMap = mapOf(LANG_AZ to LANG_AZ_KEY, LANG_EN to LANG_EN_KEY, LANG_RU to LANG_RU_KEY)

//MESSAGES AND ERRORS
const val NO_INTERNET_CONNECTION = "No internet connection"
const val NO_MESSAGE = "No message"
const val NULL_BODY = "Null body"
const val NOT_SUCCESSFUL = "Not successful"
const val EXPIRE_TOKEN = "Expired token"
const val UNKNOWN_ERROR = "An error happened"

//CONFIGURATIONS
val FITNESS_OPTIONS: FitnessOptions by lazy {
    FitnessOptions.builder()
        .addDataType(DataType.TYPE_STEP_COUNT_DELTA, FitnessOptions.ACCESS_READ)
        .addDataType(DataType.AGGREGATE_STEP_COUNT_DELTA, FitnessOptions.ACCESS_READ)
        .addDataType(DataType.TYPE_CALORIES_EXPENDED, FitnessOptions.ACCESS_READ)
        .addDataType(DataType.AGGREGATE_CALORIES_EXPENDED, FitnessOptions.ACCESS_READ)
        .addDataType(DataType.TYPE_LOCATION_SAMPLE, FitnessOptions.ACCESS_READ)
        .addDataType(DataType.AGGREGATE_LOCATION_BOUNDING_BOX, FitnessOptions.ACCESS_READ)
        .build()
}