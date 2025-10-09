# Define an array of Dockerfiles and image names
declare -A images=(
    ["game-lobby"]="games/lobby/Dockerfile"
    # ["image2"]="Dockerfile2"
    # Add more as needed: ["image_name"]="Dockerfile_path"
)

for image in "${!images[@]}"; do
    dockerfile_path="${images[$image]}"
    image_tag="${REGISTRY}/${OWNER}/${image}:latest"

    echo "Building $image_tag from $dockerfile_path"
    docker build -f "$dockerfile_path" -t "$image_tag" .

    echo "Pushing $image_tag"
    docker push "$image_tag"
done