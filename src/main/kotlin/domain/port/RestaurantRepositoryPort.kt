package domain.port
import domain.model.Restaurant
import org.springframework.stereotype.Component

@Component
interface RestaurantRepositoryPort : BaseRepositoryPort<Restaurant> {
    fun findByName(name: String): Restaurant?
}